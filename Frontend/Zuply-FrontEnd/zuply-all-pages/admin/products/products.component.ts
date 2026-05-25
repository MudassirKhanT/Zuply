// src/app/pages/admin/products/products.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-products', templateUrl:'./products.component.html', styleUrls:['./products.component.scss'] })
export class ProductsComponent implements OnInit {
  allProducts: any[] = [];
  loading     = true;
  errorMsg    = '';
  actionId: number | null = null;
  activeFilter: 'all' | 'approved' | 'pending' | 'rejected' = 'all';
  searchQuery = '';

  readonly filters: { key: string; label: string; icon: string }[] = [
    { key: 'all',      label: 'All Products',    icon: '📦' },
    { key: 'approved', label: 'Approved',         icon: '✅' },
    { key: 'pending',  label: 'Pending Review',   icon: '⏳' },
    { key: 'rejected', label: 'Rejected',         icon: '❌' },
  ];

  isPending(p: any): boolean { return p.status === 'PENDING' || p.status === 'PUBLISHED'; }

  get products(): any[] {
    const q = this.searchQuery.trim().toLowerCase();
    let list: any[];
    switch (this.activeFilter) {
      case 'approved': list = this.allProducts.filter(p => p.status === 'APPROVED'); break;
      case 'pending':  list = this.allProducts.filter(p => this.isPending(p)); break;
      case 'rejected': list = this.allProducts.filter(p => p.status === 'REJECTED'); break;
      default:         list = this.allProducts;
    }
    return q ? list.filter(p => (p.name || '').toLowerCase().includes(q) || (p.sellerName || '').toLowerCase().includes(q)) : list;
  }

  get filterCounts(): Record<string, number> {
    return {
      all:      this.allProducts.length,
      approved: this.allProducts.filter(p => p.status === 'APPROVED').length,
      pending:  this.allProducts.filter(p => this.isPending(p)).length,
      rejected: this.allProducts.filter(p => p.status === 'REJECTED').length,
    };
  }

  constructor(
    private adminService: AdminService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.errorMsg = '';

    // Read filter from query param (e.g. ?filter=approved)
    this.route.queryParams.subscribe(params => {
      const f = params['filter'];
      if (f && ['all','approved','pending','rejected'].includes(f)) {
        this.activeFilter = f as any;
      }
    });

    this.adminService.getProducts().subscribe({
      next: res => {
        if (res.success) { this.allProducts = res.data || []; }
        else { this.errorMsg = res.message || 'Failed to load products.'; }
        this.loading = false;
      },
      error: () => { this.loading = false; this.errorMsg = 'Failed to load products. The server may be waking up — please refresh in a moment.'; }
    });
  }

  setFilter(key: string): void {
    this.activeFilter = key as any;
    this.router.navigate([], { queryParams: { filter: key }, queryParamsHandling: 'merge', replaceUrl: true });
  }

  approve(id: number, source: string = 'LISTING'): void {
    this.actionId = id;
    this.adminService.approveProduct(id, source).subscribe({
      next: () => {
        this.actionId = null;
        const p = this.allProducts.find(p => p.id === id && p.source === source);
        if (p) p.status = 'APPROVED';
        this.allProducts = [...this.allProducts]; // trigger change detection
      },
      error: err => { this.actionId = null; this.errorMsg = err.error?.message || 'Failed to approve product.'; }
    });
  }

  reject(id: number, source: string = 'LISTING'): void {
    if (!confirm('Reject this product?')) return;
    this.actionId = id;
    this.adminService.rejectProduct(id, source).subscribe({
      next: () => {
        this.actionId = null;
        const p = this.allProducts.find(p => p.id === id && p.source === source);
        if (p) p.status = 'REJECTED';
        this.allProducts = [...this.allProducts]; // trigger change detection
      },
      error: err => { this.actionId = null; this.errorMsg = err.error?.message || 'Failed to reject product.'; }
    });
  }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }
}
