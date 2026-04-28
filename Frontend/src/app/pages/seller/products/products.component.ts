import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SellerService } from '../../../core/services/seller.service';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit {
  products: any[] = [];
  loading = true;
  error = '';

  constructor(
    private sellerService: SellerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.sellerService.getMyProducts().subscribe({
      next: res => {
        if (res.success) this.products = res.data;
        else this.error = res.message;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load products.';
        this.loading = false;
      }
    });
  }

  uploadNew(): void {
    this.router.navigate(['/seller/upload']);
  }

  editProduct(productId: number): void {
    this.router.navigate(['/seller/listing-preview'], { queryParams: { productId } });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PUBLISHED': return 'status-published';
      case 'DRAFT':     return 'status-draft';
      default:          return 'status-draft';
    }
  }

  formatPrice(product: any): string {
    if (product.price) return `₹${product.price}`;
    if (product.suggestedPriceMin && product.suggestedPriceMax) {
      return `₹${product.suggestedPriceMin} – ₹${product.suggestedPriceMax}`;
    }
    return 'Price not set';
  }
}
