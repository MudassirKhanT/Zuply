import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ListingService } from '../../../core/services/listing.service';

type Step = 'select' | 'processing';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})
export class UploadComponent {

  step: Step = 'select';

  // File selection
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  dragOver = false;

  // Processing state
  processingStep = 0;
  processingMessages = [
    'Uploading image...',
    'Removing background...',
    'Enhancing image quality...',
    'Generating title & description with AI...',
    'Almost done...'
  ];
  errorMsg = '';

  constructor(
    private listingService: ListingService,
    private router: Router
  ) {}

  // ── File selection ────────────────────────────────────────────────
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) this.setFile(input.files[0]);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = true;
  }

  onDragLeave(): void {
    this.dragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.dragOver = false;
    const file = event.dataTransfer?.files[0];
    if (file) this.setFile(file);
  }

  private setFile(file: File): void {
    if (!file.type.match(/image\/(jpeg|png)/)) {
      this.errorMsg = 'Only JPEG and PNG images are supported.';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.errorMsg = 'File size must be under 10 MB.';
      return;
    }
    this.errorMsg   = '';
    this.selectedFile = file;

    const reader = new FileReader();
    reader.onload = e => this.previewUrl = e.target?.result as string;
    reader.readAsDataURL(file);
  }

  removeFile(): void {
    this.selectedFile = null;
    this.previewUrl   = null;
    this.errorMsg     = '';
  }

  // ── Main action ───────────────────────────────────────────────────
  enhanceAndGenerate(): void {
    if (!this.selectedFile) return;
    this.step           = 'processing';
    this.processingStep = 0;
    this.errorMsg       = '';

    // Cycle through status messages while the backend works
    const msgInterval = setInterval(() => {
      if (this.processingStep < this.processingMessages.length - 1) {
        this.processingStep++;
      }
    }, 3500);

    this.listingService.uploadImage(this.selectedFile).subscribe({
      next: uploadRes => {
        if (!uploadRes.success) {
          clearInterval(msgInterval);
          this.showError('Upload failed: ' + uploadRes.message);
          return;
        }

        const imageId = uploadRes.data.imageId;

        this.listingService.generateListing(imageId).subscribe({
          next: listingRes => {
            clearInterval(msgInterval);
            if (listingRes.success) {
              this.router.navigate(['/seller/listing-preview', imageId]);
            } else {
              this.showError('AI generation failed: ' + listingRes.message);
            }
          },
          error: err => {
            clearInterval(msgInterval);
            this.showError(err?.error?.message || 'Something went wrong. Please try again.');
          }
        });
      },
      error: err => {
        clearInterval(msgInterval);
        this.showError(err?.error?.message || 'Upload failed. Please try again.');
      }
    });
  }

  private showError(msg: string): void {
    this.step     = 'select';
    this.errorMsg = msg;
  }

  get currentMessage(): string {
    return this.processingMessages[this.processingStep];
  }

  get progressPercent(): number {
    return Math.round(((this.processingStep + 1) / this.processingMessages.length) * 100);
  }
}
