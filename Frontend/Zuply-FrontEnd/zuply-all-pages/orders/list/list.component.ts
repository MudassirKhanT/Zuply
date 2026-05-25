// src/app/pages/orders/list/list.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { Order } from '../../../core/models';

@Component({ selector:'app-list', templateUrl:'./list.component.html', styleUrls:['./list.component.scss'] })
export class ListComponent implements OnInit {
  orders:   Order[] = [];
  filtered: Order[] = [];
  loading   = true;
  activeTab = 'ALL';
  tabs = ['ALL', 'PLACED', 'PROCESSING', 'DELIVERED', 'CANCELLED'];

  constructor(private orderService: OrderService, private router: Router) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe({
      next: res => { if (res.success) { this.orders = res.data; this.filter(); } this.loading = false; },
      error: () => this.loading = false
    });
  }

  filter(): void {
    this.filtered = this.activeTab === 'ALL'
      ? this.orders
      : this.orders.filter(o => o.status === this.activeTab);
  }

  setTab(tab: string): void { this.activeTab = tab; this.filter(); }

  viewDetail(order: Order): void { this.router.navigate(['/orders', order.orderId]); }

  writeReview(productId: number, event: MouseEvent): void {
    event.stopPropagation();
    this.router.navigate(['/products', productId], { fragment: 'reviews' });
  }

  getStatusClass(status: string): string { return `status-${status.toLowerCase()}`; }

  getStatusIcon(status: string): string {
    const icons: Record<string, string> = {
      PLACED: '📋', PROCESSING: '🔄', SHIPPED: '🚚', DELIVERED: '✅', CANCELLED: '❌'
    };
    return icons[status] || '📦';
  }

  firstImage(order: Order): string | null {
    return order.items?.[0]?.imageUrl || null;
  }

  firstProductId(order: Order): number {
    return order.items?.[0]?.productId ?? 0;
  }
}
