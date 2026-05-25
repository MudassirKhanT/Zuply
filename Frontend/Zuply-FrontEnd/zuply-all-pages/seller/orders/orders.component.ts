// src/app/pages/seller/orders/orders.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SellerService } from '../../../core/services/seller.service';

@Component({ selector:'app-orders', templateUrl:'./orders.component.html', styleUrls:['./orders.component.scss'] })
export class OrdersComponent implements OnInit {
  allOrders: any[] = [];
  loading = true;
  // 'pending' maps to PLACED status (orders awaiting action)
  activeFilter: 'all' | 'pending' | 'processing' | 'delivered' = 'all';

  readonly filters = [
    { key: 'all',        label: 'All Orders',  icon: '🛍️', colorClass: '' },
    { key: 'pending',    label: 'Pending',     icon: '⏳', colorClass: 'tab-pending'    },
    { key: 'processing', label: 'Processing',  icon: '⚙️', colorClass: 'tab-processing' },
    { key: 'delivered',  label: 'Delivered',   icon: '✅', colorClass: 'tab-delivered'  },
  ];

  // Map filter key → actual orderStatus value
  private readonly statusMap: Record<string, string> = {
    pending:    'PLACED',
    processing: 'PROCESSING',
    delivered:  'DELIVERED',
  };

  get orders(): any[] {
    if (this.activeFilter === 'all') return this.allOrders;
    const targetStatus = this.statusMap[this.activeFilter];
    return this.allOrders.filter(o => o.orderStatus === targetStatus);
  }

  get filterCounts(): Record<string, number> {
    return {
      all:        this.allOrders.length,
      pending:    this.allOrders.filter(o => o.orderStatus === 'PLACED').length,
      processing: this.allOrders.filter(o => o.orderStatus === 'PROCESSING').length,
      delivered:  this.allOrders.filter(o => o.orderStatus === 'DELIVERED').length,
    };
  }

  constructor(
    private sellerService: SellerService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const f = params['filter'];
      if (f && ['all','pending','processing','delivered'].includes(f)) {
        this.activeFilter = f as any;
      }
    });

    this.sellerService.getMyOrders().subscribe({
      next: res => { if (res.success) this.allOrders = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  setFilter(key: string): void {
    this.activeFilter = key as any;
    this.router.navigate([], { queryParams: { filter: key }, queryParamsHandling: 'merge', replaceUrl: true });
  }

  updateStatus(orderId: number, status: string): void {
    this.sellerService.updateOrderStatus(orderId, status).subscribe({
      next: () => this.ngOnInit(),
      error: err => alert(err?.error?.message || 'Failed to update order status.')
    });
  }

  getStatusClass(status: string): string { return `status-${status.toLowerCase()}`; }

  getEmptyMessage(): string {
    switch (this.activeFilter) {
      case 'pending':    return 'No orders are currently awaiting action.';
      case 'processing': return 'No orders are currently being processed.';
      case 'delivered':  return 'No orders have been delivered yet.';
      default:           return 'Orders from customers will appear here.';
    }
  }
}
