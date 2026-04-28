// src/app/pages/seller/listing-preview/listing-preview.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ListingService } from '../../../core/services/listing.service';
import { ListingResponse } from '../../../core/models';

@Component({ selector:'app-listing-preview', templateUrl:'./listing-preview.component.html', styleUrls:['./listing-preview.component.scss'] })
export class ListingPreviewComponent implements OnInit {

  listing: ListingResponse | null = null;
  loading    = true;
  publishing = false;
  errorMsg   = '';
  successMsg = '';
  editMode   = false;

  editTitle       = '';
  editDescription = '';
  editPrice: number | null = null;
  editCategory    = '';
  editTagsRaw     = '';       // comma-separated string for easy editing
  editHighlightsRaw = '';     // one highlight per line

  constructor(
    private route: ActivatedRoute,
    private listingService: ListingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const imageId = Number(this.route.snapshot.paramMap.get('imageId'));
    this.listingService.getListing(imageId).subscribe({
      next: res => {
        if (res.success) {
          this.listing = res.data;
          this.populateEditFields(res.data);
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  private populateEditFields(data: ListingResponse): void {
    this.editTitle          = data.title;
    this.editDescription    = data.description;
    this.editPrice          = data.price || null;
    this.editCategory       = data.category;
    this.editTagsRaw        = (data.tags || []).join(', ');
    this.editHighlightsRaw  = (data.highlights || []).join('\n');
  }

  openEdit(): void {
    if (this.listing) this.populateEditFields(this.listing);
    this.editMode = true;
  }

  saveEdit(): void {
    if (!this.listing) return;

    const tags       = this.editTagsRaw.split(',').map(t => t.trim().toLowerCase()).filter(t => t.length > 0);
    const highlights = this.editHighlightsRaw.split('\n').map(h => h.trim()).filter(h => h.length > 0);

    this.listingService.editListing(this.listing.productId, {
      title:       this.editTitle,
      description: this.editDescription,
      price:       this.editPrice || undefined,
      category:    this.editCategory,
      tags,
      highlights,
    }).subscribe({
      next: res => {
        if (res.success) { this.listing = res.data; this.editMode = false; }
        else this.errorMsg = res.message;
      },
      error: err => { this.errorMsg = err?.error?.message || 'Save failed.'; }
    });
  }

  publish(): void {
    if (!this.listing) return;
    if (!this.listing.price) {
      this.errorMsg = 'Please set a price before publishing.';
      this.editMode = true;
      return;
    }
    this.publishing = true; this.errorMsg = '';
    this.listingService.publishListing(this.listing.productId).subscribe({
      next: res => {
        this.publishing = false;
        if (res.success) {
          this.successMsg = 'Listing submitted for admin approval. It will appear on the marketplace once approved.';
          if (this.listing) this.listing.status = 'PENDING';
        } else {
          this.errorMsg = res.message || 'Publish failed.';
        }
      },
      error: err => { this.publishing = false; this.errorMsg = err.error?.message || 'Publish failed.'; }
    });
  }
}
