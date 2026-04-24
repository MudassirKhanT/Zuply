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

  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.selectedFile = input.files[0];
    const reader = new FileReader();
    reader.onload = () => this.previewUrl = reader.result as string;
    reader.readAsDataURL(this.selectedFile);
    this.errorMsg = '';
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file && file.type.startsWith('image/')) {
      const dt = new DataTransfer();
      dt.items.add(file);
      const fakeEvent = { target: { files: dt.files } } as unknown as Event;
      this.onFileSelect(fakeEvent);
    }
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
