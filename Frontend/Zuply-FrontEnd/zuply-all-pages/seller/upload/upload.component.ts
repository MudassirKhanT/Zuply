import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ListingService } from '../../../core/services/listing.service';
import { ProductService } from '../../../core/services/product.service';
import { Category, ImageStatus } from '../../../core/models';

type UploadMode = 'manual' | 'ai';

export interface ImageItem {
  id: string;
  file: File;
  previewUrl: string;
}

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})
export class UploadComponent implements OnInit {

  mode: UploadMode = 'manual';

  images: ImageItem[] = [];
  dragIndex    = -1;
  dragOverIdx  = -1;
  zoneDragOver = false;

  uploading  = false;
  errorMsg   = '';
  successMsg = '';

  currentStatus: ImageStatus | null = null;
  statusMsg  = '';
  statusSteps: ImageStatus[] = ['PENDING', 'PROCESSING', 'PROCESSED', 'COMPLETED'];

  categories: Category[] = [];
  form = {
    name:           '',
    description:    '',
    categoryId:     null as number | null,
    price:          null as number | null,
    stock:          null as number | null,
    variations:     '',
    deliveryMethod: '',
    returnPolicy:   ''
  };

  constructor(
    private listingService: ListingService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getCategories().subscribe({
      next: res => { if (res.success) this.categories = res.data; }
    });
  }

  switchMode(m: UploadMode): void {
    this.mode      = m;
    this.errorMsg  = '';
    this.successMsg = '';
  }

  // ── File input / zone drop ────────────────────────────────────────────────────

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.addFiles(Array.from(input.files));
    input.value = '';
  }

  onZoneDrop(event: DragEvent): void {
    event.preventDefault();
    this.zoneDragOver = false;
    if (!event.dataTransfer?.files.length) return;
    const files = Array.from(event.dataTransfer.files).filter(f => f.type.startsWith('image/'));
    this.addFiles(files);
  }

  onZoneDragOver(e: DragEvent): void  { e.preventDefault(); this.zoneDragOver = true; }
  onZoneDragLeave(e: DragEvent): void { e.preventDefault(); this.zoneDragOver = false; }

  private addFiles(files: File[]): void {
    const limit = 8;
    const canAdd = limit - this.images.length;
    if (canAdd <= 0) { this.errorMsg = 'Maximum 8 images allowed.'; return; }
    const toAdd = files.slice(0, canAdd);
    if (files.length > canAdd) this.errorMsg = `Max 8 images. ${files.length - canAdd} file(s) skipped.`;
    for (const file of toAdd) {
      const item: ImageItem = { id: Math.random().toString(36).slice(2), file, previewUrl: '' };
      const reader = new FileReader();
      reader.onload = () => (item.previewUrl = reader.result as string);
      reader.readAsDataURL(file);
      this.images.push(item);
    }
  }

  removeImage(index: number, event: Event): void {
    event.stopPropagation();
    this.images.splice(index, 1);
  }

  // ── Drag-and-drop reordering ─────────────────────────────────────────────────

  onItemDragStart(index: number, event: DragEvent): void {
    this.dragIndex = index;
    event.dataTransfer!.effectAllowed = 'move';
  }

  onItemDragOver(event: DragEvent, index: number): void {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
    this.dragOverIdx = index;
  }

  onItemDrop(event: DragEvent, index: number): void {
    event.preventDefault();
    if (this.dragIndex !== -1 && this.dragIndex !== index) {
      const moved = this.images.splice(this.dragIndex, 1)[0];
      this.images.splice(index, 0, moved);
    }
    this.dragIndex   = -1;
    this.dragOverIdx = -1;
  }

  onItemDragEnd(): void {
    this.dragIndex   = -1;
    this.dragOverIdx = -1;
  }

  // ── Manual submit ─────────────────────────────────────────────────────────────

  submitManual(): void {
    if (!this.validateManual()) return;
    this.uploading = true;
    this.errorMsg  = '';
    this.uploadAllImages().then(urls => {
      this.createManualProduct(urls[0] ?? '', urls.slice(1));
    }).catch(() => {
      this.uploading = false;
      this.errorMsg  = 'Image upload failed. Please try again.';
    });
  }

  private async uploadAllImages(): Promise<string[]> {
    const urls: string[] = [];
    for (const item of this.images) {
      const res = await this.listingService.uploadImage(item.file).toPromise();
      urls.push(res?.data?.imageUrl ?? res?.data?.originalUrl ?? '');
    }
    return urls;
  }

  private createManualProduct(imageUrl: string, extraImages: string[]): void {
    this.productService.createProduct({
      name:           this.form.name,
      description:    this.form.description || undefined,
      categoryId:     this.form.categoryId!,
      price:          this.form.price!,
      stock:          this.form.stock!,
      imageUrl:       imageUrl || undefined,
      extraImages:    extraImages.length > 0 ? extraImages : undefined,
      variations:     this.form.variations || undefined,
      deliveryMethod: this.form.deliveryMethod || undefined,
      returnPolicy:   this.form.returnPolicy || undefined
    }).subscribe({
      next: res => {
        this.uploading = false;
        if (res.success) {
          this.successMsg = 'Product submitted for review!';
          setTimeout(() => this.router.navigate(['/seller/products']), 1500);
        } else {
          this.errorMsg = res.message || 'Failed to create product.';
        }
      },
      error: err => {
        this.uploading = false;
        this.errorMsg  = err.error?.message || 'Failed to create product.';
      }
    });
  }

  private validateManual(): boolean {
    if (!this.form.name.trim())   { this.errorMsg = 'Product name is required.';  return false; }
    if (!this.form.categoryId)    { this.errorMsg = 'Please select a category.';  return false; }
    if (!this.form.price || this.form.price <= 0) { this.errorMsg = 'Enter a valid price.'; return false; }
    if (this.form.stock === null || this.form.stock < 0) { this.errorMsg = 'Enter valid stock.'; return false; }
    return true;
  }

  // ── AI mode ───────────────────────────────────────────────────────────────────

  generateWithAI(): void {
    if (!this.images.length) { this.errorMsg = 'Please add at least one image.'; return; }
    this.uploading     = true;
    this.errorMsg      = '';
    this.statusMsg     = 'Uploading primary image...';
    this.currentStatus = 'PENDING';

    this.listingService.uploadImage(this.images[0].file).subscribe({
      next: res => {
        if (res.success) {
          this.statusMsg     = 'AI is analyzing your product...';
          this.currentStatus = 'PROCESSING';
          this.runGenerateListing(res.data.imageId);
        } else {
          this.uploading = false;
          this.errorMsg  = res.message || 'Upload failed.';
        }
      },
      error: err => {
        this.uploading = false;
        this.errorMsg  = err.error?.message || 'Upload failed.';
      }
    });
  }

  private runGenerateListing(imageId: number): void {
    this.listingService.generateListing(imageId).subscribe({
      next: async res => {
        if (res.success) {
          this.currentStatus = 'COMPLETED';
          this.statusMsg     = 'Done! Redirecting to preview...';

          if (this.images.length > 1) {
            try {
              const extraUrls: string[] = [];
              for (const item of this.images.slice(1)) {
                const up = await this.listingService.uploadImage(item.file).toPromise();
                const url = up?.data?.imageUrl ?? up?.data?.originalUrl ?? '';
                if (url) extraUrls.push(url);
              }
              if (extraUrls.length > 0)
                await this.listingService.saveExtraImages(res.data.productId, extraUrls).toPromise();
            } catch { /* extra images are non-critical */ }
          }

          setTimeout(() => this.router.navigate(['/seller/listing-preview', imageId]), 1000);
        } else {
          this.uploading = false;
          this.errorMsg  = res.message || 'AI generation failed.';
        }
      },
      error: err => {
        this.uploading = false;
        this.errorMsg  = err.error?.message || 'AI generation failed.';
      }
    });
  }

  getStepClass(step: ImageStatus): string {
    const idx = this.statusSteps.indexOf(step);
    const cur = this.statusSteps.indexOf(this.currentStatus ?? 'PENDING');
    if (idx < cur)   return 'step-done';
    if (idx === cur) return 'step-active';
    return 'step-pending';
  }
}
