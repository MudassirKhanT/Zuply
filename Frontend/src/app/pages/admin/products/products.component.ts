import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit {
  products: any[] = [];
  loading = true;
  error = '';
  actionMessage = '';

  editingProduct: any = null;
  editForm: any = {};

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.adminService.getProducts().subscribe({
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

  openEdit(product: any): void {
    this.editingProduct = product;
    this.editForm = {
      title: product.title,
      description: product.description,
      category: product.category,
      price: product.price,
      status: product.status
    };
  }

  saveEdit(): void {
    if (!this.editingProduct) return;
    this.adminService.updateProduct(this.editingProduct.id, this.editForm).subscribe({
      next: res => {
        if (res.success) {
          this.actionMessage = 'Product updated.';
          this.editingProduct = null;
          this.loadProducts();
        }
      },
      error: () => this.actionMessage = 'Update failed.'
    });
  }

  cancelEdit(): void {
    this.editingProduct = null;
  }

  deleteProduct(id: number): void {
    if (!confirm('Delete this product? This cannot be undone.')) return;
    this.adminService.deleteProduct(id).subscribe({
      next: res => {
        if (res.success) {
          this.actionMessage = 'Product deleted.';
          this.loadProducts();
        }
      },
      error: () => this.actionMessage = 'Delete failed.'
    });
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
    if (product.suggestedPriceMin) return `₹${product.suggestedPriceMin}–${product.suggestedPriceMax}`;
    return '—';
  }
}
