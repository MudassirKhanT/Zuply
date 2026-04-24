// src/app/pages/seller/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { SellerService } from '../../../core/services/seller.service';

@Component({ selector:'app-dashboard', templateUrl:'./dashboard.component.html', styleUrls:['./dashboard.component.scss'] })
export class DashboardComponent implements OnInit {
  stats: any    = null;
  orders: any[] = [];
  loading       = true;
  userName      = '';

  constructor(private sellerService: SellerService, private auth: AuthService) {}

  ngOnInit(): void {
    this.userName = this.auth.getUserName();
    this.sellerService.getDashboard().subscribe({
      next: res => { if (res.success) this.stats = res.data; this.loading = false; },
      error: () => this.loading = false
    });
    this.sellerService.getMyOrders().subscribe({
      next: res => { if (res.success) this.orders = res.data.slice(0, 5); }
    });
  }

  getStatusClass(status: string): string { return `status-${status.toLowerCase()}`; }
}
