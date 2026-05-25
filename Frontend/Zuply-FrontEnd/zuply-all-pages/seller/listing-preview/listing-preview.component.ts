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

  editTitle          = '';
  editDescription    = '';
  editPrice: number | null = null;
  editStock: number | null = null;
  editCategory       = '';
  editDeliveryMethod = '';
  editReturnPolicy   = '';

  readonly deliveryOptions = ['Home Delivery', 'Store Pickup'];
  readonly returnOptions   = ['No Returns', '7-Day Returns', '15-Day Returns', '30-Day Returns'];

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
          this.listing           = res.data;
          this.editTitle         = res.data.title;
          this.editDescription   = res.data.description;
          this.editPrice         = res.data.price  ?? null;
          this.editStock         = res.data.stock  ?? null;
          this.editCategory      = res.data.category;
          this.editDeliveryMethod = res.data.deliveryMethod ?? '';
          this.editReturnPolicy   = res.data.returnPolicy   ?? '';
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  saveEdit(): void {
    if (!this.listing) return;
    if (!this.editDeliveryMethod) { this.errorMsg = 'Please select a delivery method.'; return; }
    if (!this.editReturnPolicy)   { this.errorMsg = 'Please select a return policy.';   return; }
    this.listingService.editListing(this.listing.productId, {
      title:          this.editTitle,
      description:    this.editDescription,
      price:          this.editPrice    ?? undefined,
      stock:          this.editStock    ?? undefined,
      category:       this.editCategory,
      deliveryMethod: this.editDeliveryMethod,
      returnPolicy:   this.editReturnPolicy,
    }).subscribe({
      next: res => {
        if (res.success) {
          this.listing  = res.data;
          this.editStock = res.data.stock ?? null;
          this.editMode  = false;
        } else this.errorMsg = res.message;
      }
    });
  }

  publish(): void {
    if (!this.listing) return;
    if (!this.listing.price) { this.errorMsg = 'Please set a price before publishing.'; return; }
    if (this.listing.stock === null || this.listing.stock === undefined) {
      this.errorMsg = 'Please set a stock quantity before publishing.';
      this.editMode = true; return;
    }
    if (!this.editDeliveryMethod) { this.errorMsg = 'Please select a delivery method before publishing.'; this.editMode = true; return; }
    if (!this.editReturnPolicy)   { this.errorMsg = 'Please select a return policy before publishing.';   this.editMode = true; return; }
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
