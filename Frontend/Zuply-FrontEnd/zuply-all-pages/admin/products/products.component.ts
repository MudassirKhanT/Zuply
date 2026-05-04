// src/app/pages/admin/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  products: any[] = [];
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getProducts().subscribe({
      next: res => { if (res.success) this.products = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  approve(id: number): void {
    this.adminService.approveProduct(id).subscribe(() => this.ngOnInit());
  }

  reject(id: number): void {
    if (!confirm('Reject this product?')) return;
    this.adminService.rejectProduct(id).subscribe(() => this.ngOnInit());
  }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }
}
