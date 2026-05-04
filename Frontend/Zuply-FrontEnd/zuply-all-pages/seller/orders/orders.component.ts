// src/app/pages/seller/orders/orders.component.ts
import { Component, OnInit } from '@angular/core';
import { SellerService } from '../../../core/services/seller.service';

@Component({ selector:'app-orders', templateUrl:'./orders.component.html', styleUrls:['./orders.component.scss'] })
export class OrdersComponent implements OnInit {
  orders: any[] = [];
  loading = true;

  constructor(private sellerService: SellerService) {}

  ngOnInit(): void {
    this.sellerService.getMyOrders().subscribe({
      next: res => { if (res.success) this.orders = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  updateStatus(orderId: number, status: string): void {
    this.sellerService.updateOrderStatus(orderId, status).subscribe({
      next: () => this.ngOnInit()
    });
  }

  getStatusClass(status: string): string { return `status-${status.toLowerCase()}`; }
}
