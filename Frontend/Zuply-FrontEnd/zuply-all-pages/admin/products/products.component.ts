// src/app/pages/admin/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  products: any[] = [];
  loading  = true;
  errorMsg = '';
  actionId: number | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.errorMsg = '';
    this.adminService.getProducts().subscribe({
      next: res => { if (res.success) this.products = res.data; this.loading = false; },
      error: () => { this.loading = false; this.errorMsg = 'Failed to load products.'; }
    });
  }

  approve(id: number): void {
    this.actionId = id;
    this.adminService.approveProduct(id).subscribe({
      next: () => { this.actionId = null; this.ngOnInit(); },
      error: err => { this.actionId = null; this.errorMsg = err.error?.message || 'Failed to approve product.'; }
    });
  }

  reject(id: number): void {
    if (!confirm('Reject this product?')) return;
    this.actionId = id;
    this.adminService.rejectProduct(id).subscribe({
      next: () => { this.actionId = null; this.ngOnInit(); },
      error: err => { this.actionId = null; this.errorMsg = err.error?.message || 'Failed to reject product.'; }
    });
  }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }
}
