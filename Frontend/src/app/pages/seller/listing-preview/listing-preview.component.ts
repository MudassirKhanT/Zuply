import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ListingService } from '../../../core/services/listing.service';
import { ListingResponse, ListingEditRequest } from '../../../core/models';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-listing-preview',
  templateUrl: './listing-preview.component.html',
  styleUrls: ['./listing-preview.component.scss']
})
export class ListingPreviewComponent implements OnInit {

  listing: ListingResponse | null = null;
  loading    = true;
  publishing = false;
  saving     = false;
  published  = false;
  errorMsg   = '';
  successMsg = '';

  // Editable fields bound to the form
  editTitle       = '';
  editDescription = '';
  editCategory    = '';
  editPrice: number | null = null;

  readonly categories = [
    'Electronics', 'Clothing', 'Grocery', 'Food & Beverage',
    'Home & Kitchen', 'Beauty & Personal Care', 'Health & Wellness',
    'Agriculture', 'Fashion & Footwear'
  ];

  readonly apiBase = environment.apiUrl.replace('/api', '');

  constructor(
    private route:          ActivatedRoute,
    private router:         Router,
    private listingService: ListingService
  ) {}

  ngOnInit(): void {
    const imageId = Number(this.route.snapshot.paramMap.get('imageId'));
    this.listingService.getListing(imageId).subscribe({
      next: res => {
        if (res.success) {
          this.listing          = res.data;
          this.editTitle        = res.data.title        || '';
          this.editDescription  = res.data.description  || '';
          this.editCategory     = res.data.category     || '';
          this.editPrice        = res.data.price        ?? null;
        } else {
          this.errorMsg = 'Could not load listing.';
        }
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Failed to load listing. Please go back and try again.';
        this.loading  = false;
      }
    });
  }

  // ── Save edits ────────────────────────────────────────────────────
  saveEdits(): void {
    if (!this.listing) return;
    this.saving    = true;
    this.errorMsg  = '';
    this.successMsg = '';

    const req: ListingEditRequest = {
      title:       this.editTitle,
      description: this.editDescription,
      category:    this.editCategory,
      price:       this.editPrice ?? undefined
    };

    this.listingService.editListing(this.listing.productId, req).subscribe({
      next: res => {
        this.saving = false;
        if (res.success) {
          this.listing    = res.data;
          this.successMsg = 'Changes saved.';
          setTimeout(() => this.successMsg = '', 3000);
        } else {
          this.errorMsg = res.message || 'Save failed.';
        }
      },
      error: err => {
        this.saving   = false;
        this.errorMsg = err?.error?.message || 'Save failed.';
      }
    });
  }

  // ── Publish ───────────────────────────────────────────────────────
  publish(): void {
    if (!this.listing) return;
    if (!this.editPrice || this.editPrice <= 0) {
      this.errorMsg = 'Please set a valid price before publishing.';
      return;
    }

    // Save latest edits first, then publish
    this.publishing = true;
    this.errorMsg   = '';

    const req: ListingEditRequest = {
      title:       this.editTitle,
      description: this.editDescription,
      category:    this.editCategory,
      price:       this.editPrice
    };

    this.listingService.editListing(this.listing.productId, req).subscribe({
      next: saveRes => {
        if (!saveRes.success) {
          this.publishing = false;
          this.errorMsg   = 'Could not save changes before publishing.';
          return;
        }
        this.listingService.publishListing(this.listing!.productId).subscribe({
          next: pubRes => {
            this.publishing = false;
            if (pubRes.success) {
              this.published = true;
            } else {
              this.errorMsg = pubRes.message || 'Publishing failed.';
            }
          },
          error: err => {
            this.publishing = false;
            this.errorMsg   = err?.error?.message || 'Publishing failed.';
          }
        });
      },
      error: err => {
        this.publishing = false;
        this.errorMsg   = err?.error?.message || 'Save failed.';
      }
    });
  }

  // ── Navigation ────────────────────────────────────────────────────
  uploadAnother(): void {
    this.router.navigate(['/seller/upload']);
  }

  goToDashboard(): void {
    this.router.navigate(['/seller/dashboard']);
  }

  // ── Helpers ───────────────────────────────────────────────────────
  imageUrl(path: string | undefined): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return this.apiBase + path;
  }

  priceRange(): string {
    const { suggestedPriceMin: min, suggestedPriceMax: max } = this.listing!;
    if (min && max) return `₹${min} – ₹${max}`;
    if (min)        return `from ₹${min}`;
    if (max)        return `up to ₹${max}`;
    return '';
  }
}
