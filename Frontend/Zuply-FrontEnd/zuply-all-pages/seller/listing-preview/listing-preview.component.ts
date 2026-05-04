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
          this.listing      = res.data;
          this.editTitle    = res.data.title;
          this.editDescription = res.data.description;
          this.editPrice    = res.data.price || null;
          this.editCategory = res.data.category;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  saveEdit(): void {
    if (!this.listing) return;
    this.listingService.editListing(this.listing.productId, {
      title: this.editTitle,
      description: this.editDescription,
      price: this.editPrice || undefined,
      category: this.editCategory,
    }).subscribe({
      next: res => {
        if (res.success) { this.listing = res.data; this.editMode = false; }
        else this.errorMsg = res.message;
      }
    });
  }

  publish(): void {
    if (!this.listing) return;
    this.publishing = true; this.errorMsg = '';
    this.listingService.publishListing(this.listing.productId).subscribe({
      next: res => {
        this.publishing = false;
        if (res.success) {
          this.successMsg = 'Product published successfully!';
          setTimeout(() => this.router.navigate(['/seller/products']), 1500);
        } else {
          this.errorMsg = res.message || 'Publish failed.';
        }
      },
      error: err => { this.publishing = false; this.errorMsg = err.error?.message || 'Publish failed.'; }
    });
  }
}
