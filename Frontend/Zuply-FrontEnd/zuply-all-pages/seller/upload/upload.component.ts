// src/app/pages/seller/upload/upload.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ListingService } from '../../../core/services/listing.service';
import { ImageStatus } from '../../../core/models';

@Component({ selector:'app-upload', templateUrl:'./upload.component.html', styleUrls:['./upload.component.scss'] })
export class UploadComponent {

  selectedFile: File | null = null;
  previewUrl: string | null = null;
  uploading   = false;
  processing  = false;
  errorMsg    = '';
  statusMsg   = '';
  currentStatus: ImageStatus | null = null;

  statusSteps: ImageStatus[] = ['PENDING','PROCESSING','PROCESSED','COMPLETED'];

  constructor(private listingService: ListingService, private router: Router) {}

  private readonly ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png'];
  private readonly MAX_SIZE_MB   = 10;

  private validateFile(file: File): string | null {
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      return 'Only JPG and PNG images are accepted. Please select a valid file.';
    }
    if (file.size > this.MAX_SIZE_MB * 1024 * 1024) {
      return `File size exceeds ${this.MAX_SIZE_MB} MB. Please choose a smaller image.`;
    }
    return null;
  }

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    const err  = this.validateFile(file);
    if (err) { this.errorMsg = err; this.selectedFile = null; this.previewUrl = null; return; }
    this.selectedFile = file;
    this.errorMsg = '';
    const reader = new FileReader();
    reader.onload = () => this.previewUrl = reader.result as string;
    reader.readAsDataURL(this.selectedFile);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (!file) return;
    const err = this.validateFile(file);
    if (err) { this.errorMsg = err; return; }
    const dt = new DataTransfer();
    dt.items.add(file);
    const fakeEvent = { target: { files: dt.files } } as unknown as Event;
    this.onFileSelect(fakeEvent);
  }

  onDragOver(event: DragEvent): void { event.preventDefault(); }

  upload(): void {
    if (!this.selectedFile) { this.errorMsg = 'Please select an image first.'; return; }
    this.uploading  = true;
    this.errorMsg   = '';
    this.statusMsg  = 'Uploading image...';
    this.currentStatus = 'PENDING';

    this.listingService.uploadImage(this.selectedFile).subscribe({
      next: res => {
        if (res.success) {
          const imageId = res.data.imageId;
          this.statusMsg = 'Generating AI listing...';
          this.currentStatus = 'PROCESSING';
          this.generateListing(imageId);
        } else {
          this.uploading = false;
          this.errorMsg  = res.message || 'Upload failed.';
        }
      },
      error: err => {
        this.uploading = false;
        this.errorMsg  = err.error?.message || 'Upload failed. Please try again.';
      }
    });
  }

  generateListing(imageId: number): void {
    this.listingService.generateListing(imageId).subscribe({
      next: res => {
        this.uploading = false;
        if (res.success) {
          this.currentStatus = 'COMPLETED';
          this.statusMsg = 'Listing generated! Redirecting...';
          setTimeout(() => this.router.navigate(['/seller/listing-preview', imageId]), 1000);
        } else {
          this.errorMsg = res.message || 'AI generation failed.';
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
    const cur = this.statusSteps.indexOf(this.currentStatus || 'PENDING');
    if (idx < cur)  return 'step-done';
    if (idx === cur) return 'step-active';
    return 'step-pending';
  }
}
