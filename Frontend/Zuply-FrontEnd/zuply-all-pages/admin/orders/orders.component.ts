// src/app/pages/admin/orders/orders.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class AdminOrdersComponent implements OnInit {
  allOrders: any[] = [];
  loading   = true;
  errorMsg  = '';
  activeFilter: 'all' | 'PLACED' | 'PROCESSING' | 'DELIVERED' | 'CANCELLED' = 'all';

  readonly filters = [
    { key: 'all',        label: 'All Orders',  icon: '🛍️' },
    { key: 'PLACED',     label: 'Placed',      icon: '📋' },
    { key: 'PROCESSING', label: 'Processing',  icon: '⚙️' },
    { key: 'DELIVERED',  label: 'Delivered',   icon: '✅' },
    { key: 'CANCELLED',  label: 'Cancelled',   icon: '❌' },
  ];

  get orders(): any[] {
    if (this.activeFilter === 'all') return this.allOrders;
    return this.allOrders.filter(o => o.status === this.activeFilter);
  }

  get filterCounts(): Record<string, number> {
    const counts: Record<string, number> = { all: this.allOrders.length };
    ['PLACED','PROCESSING','DELIVERED','CANCELLED'].forEach(s => {
      counts[s] = this.allOrders.filter(o => o.status === s).length;
    });
    return counts;
  }

  get totalRevenue(): number {
    return this.orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0);
  }

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getOrders().subscribe({
      next: res => { if (res.success) this.allOrders = res.data; this.loading = false; },
      error: () => { this.loading = false; this.errorMsg = 'Failed to load orders.'; }
    });
  }

  setFilter(key: string): void { this.activeFilter = key as any; }

  getStatusClass(status: string): string { return `status-${(status || 'placed').toLowerCase()}`; }

  getStatusIcon(status: string): string {
    const map: Record<string, string> = {
      PLACED: '📋', PROCESSING: '⚙️', DELIVERED: '✅', CANCELLED: '❌'
    };
    return map[status] || '📋';
  }

  formatDate(iso: string): string {
    if (!iso) return '—';
    try { return new Date(iso).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' }); }
    catch { return iso; }
  }
}
