// src/app/pages/seller/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SellerService } from '../../../core/services/seller.service';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  allProducts: Product[] = [];
  loading = true;
  activeFilter: 'all' | 'approved' | 'pending' | 'rejected' = 'all';
  editingStockId: number | null = null;
  editStockValue: number | null = null;

  readonly filters = [
    { key: 'all',      label: 'All Products',   icon: '📦', colorClass: '' },
    { key: 'approved', label: 'Approved',        icon: '✅', colorClass: 'tab-approved' },
    { key: 'pending',  label: 'Pending Review',  icon: '⏳', colorClass: 'tab-pending'  },
    { key: 'rejected', label: 'Rejected',        icon: '❌', colorClass: 'tab-rejected' },
  ];

  get products(): Product[] {
    switch (this.activeFilter) {
      case 'approved': return this.allProducts.filter(p => p.status === 'APPROVED');
      case 'pending':  return this.allProducts.filter(p => p.status === 'PENDING');
      case 'rejected': return this.allProducts.filter(p => p.status === 'REJECTED');
      default:         return this.allProducts;
    }
  }

  get filterCounts(): Record<string, number> {
    return {
      all:      this.allProducts.length,
      approved: this.allProducts.filter(p => p.status === 'APPROVED').length,
      pending:  this.allProducts.filter(p => p.status === 'PENDING').length,
      rejected: this.allProducts.filter(p => p.status === 'REJECTED').length,
    };
  }

  constructor(
    private sellerService: SellerService,
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const f = params['filter'];
      if (f && ['all','approved','pending','rejected'].includes(f)) {
        this.activeFilter = f as any;
      }
    });

    this.sellerService.getMyProducts().subscribe({
      next: res => { if (res.success) this.allProducts = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  setFilter(key: string): void {
    this.activeFilter = key as any;
    this.router.navigate([], { queryParams: { filter: key }, queryParamsHandling: 'merge', replaceUrl: true });
  }

  deleteProduct(id: number): void {
    if (!confirm('Delete this product?')) return;
    this.productService.delete(id).subscribe({
      next: () => this.ngOnInit(),
      error: err => alert(err?.error?.message || 'Failed to delete product.')
    });
  }

  startEditStock(p: Product): void {
    this.editingStockId = p.id;
    this.editStockValue = p.stock ?? 0;
  }

  saveStock(p: Product): void {
    if (this.editStockValue === null || this.editStockValue < 0) return;
    this.productService.updateStock(p.id, this.editStockValue).subscribe({
      next: () => {
        p.stock = this.editStockValue!;
        this.editingStockId = null;
      },
      error: err => alert(err?.error?.message || 'Failed to update stock.')
    });
  }

  cancelEditStock(): void { this.editingStockId = null; }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }

  getFilterTitle(): string {
    switch (this.activeFilter) {
      case 'approved': return 'Approved Products';
      case 'pending':  return 'Pending Review';
      case 'rejected': return 'Rejected Products';
      default:         return 'All Products';
    }
  }
}
