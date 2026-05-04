// src/app/pages/seller/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { SellerService } from '../../../core/services/seller.service';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  products: Product[] = [];
  loading = true;

  constructor(private sellerService: SellerService, private productService: ProductService) {}

  ngOnInit(): void {
    this.sellerService.getMyProducts().subscribe({
      next: res => { if (res.success) this.products = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  deleteProduct(id: number): void {
    if (!confirm('Delete this product?')) return;
    this.productService.delete(id).subscribe(() => this.ngOnInit());
  }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }
}
