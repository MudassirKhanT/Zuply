// src/app/pages/seller/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SellerService } from '../../../core/services/seller.service';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  products: any[] = [];
  loading = true;

  constructor(private sellerService: SellerService, private router: Router) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.sellerService.getMyProducts().subscribe({
      next: res => { if (res.success) this.products = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  editProduct(productId: number): void {
    this.router.navigate(['/seller/listing-preview'], { queryParams: { productId } });
  }

  getStatusClass(status: string): string { return `status-${(status || 'draft').toLowerCase()}`; }

  formatPrice(p: any): string {
    if (p.price) return `₹ ${p.price}`;
    if (p.suggestedPriceMin) return `₹ ${p.suggestedPriceMin} – ₹ ${p.suggestedPriceMax}`;
    return 'Price not set';
  }
}
