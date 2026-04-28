// src/app/pages/admin/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  products: any[] = [];
  loading = true;
  actionMsg = '';

  editingProduct: any = null;
  editForm: any = {};

  constructor(private adminService: AdminService) {}

  ngOnInit(): void { this.loadProducts(); }

  loadProducts(): void {
    this.loading = true;
    this.adminService.getProducts().subscribe({
      next: res => { if (res.success) this.products = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  approve(id: number): void {
    this.adminService.approveProduct(id).subscribe({
      next: res => {
        if (res.success) { this.actionMsg = '✅ Product approved and published to marketplace.'; this.loadProducts(); }
      },
      error: () => this.actionMsg = '❌ Approval failed.'
    });
  }

  reject(id: number): void {
    if (!confirm('Reject this product? The seller will need to resubmit.')) return;
    this.adminService.rejectProduct(id).subscribe({
      next: res => {
        if (res.success) { this.actionMsg = 'Product rejected.'; this.loadProducts(); }
      },
      error: () => this.actionMsg = '❌ Rejection failed.'
    });
  }

  openEdit(product: any): void {
    this.editingProduct = product;
    this.editForm = {
      title:       product.title,
      description: product.description,
      category:    product.category,
      price:       product.price,
      status:      product.status
    };
  }

  saveEdit(): void {
    if (!this.editingProduct) return;
    this.adminService.updateProduct(this.editingProduct.id, this.editForm).subscribe({
      next: res => {
        if (res.success) { this.actionMsg = 'Product updated.'; this.editingProduct = null; this.loadProducts(); }
      },
      error: () => this.actionMsg = '❌ Update failed.'
    });
  }

  cancelEdit(): void { this.editingProduct = null; }

  deleteProduct(id: number): void {
    if (!confirm('Delete this product? This cannot be undone.')) return;
    this.adminService.deleteProduct(id).subscribe({
      next: res => { if (res.success) { this.actionMsg = 'Product deleted.'; this.loadProducts(); } },
      error: () => this.actionMsg = '❌ Delete failed.'
    });
  }

  getStatusClass(status: string): string { return `status-${(status || 'draft').toLowerCase()}`; }

  formatPrice(p: any): string {
    if (p.price) return `₹ ${p.price}`;
    if (p.suggestedPriceMin) return `₹ ${p.suggestedPriceMin}–${p.suggestedPriceMax}`;
    return '—';
  }

  isPending(p: any): boolean {
    const s = (p.status || '').toUpperCase();
    return s === 'PENDING' || s === 'PUBLISHED'; // PUBLISHED is the legacy pre-fix status
  }
}
