import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { SellerService } from '../../../core/services/seller.service';
import { Product, SellerDashboard, SellerOrder } from '../../../core/models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  stats: SellerDashboard | null = null;
  recentOrders: SellerOrder[] = [];
  products: Product[] = [];
  loading     = true;
  setupBanner = false;
  userName    = '';

  ordersPage     = 1;
  ordersPageSize = 5;

  get approvedProducts(): number  { return this.products.filter(p => p.status === 'APPROVED').length; }
  get pendingProducts(): number   { return this.products.filter(p => p.status === 'PENDING').length; }
  get rejectedProducts(): number  { return this.products.filter(p => p.status === 'REJECTED').length; }
  get approvalRate(): number {
    if (!this.products.length) return 0;
    return Math.round((this.approvedProducts / this.products.length) * 100);
  }

  get pagedOrders(): SellerOrder[] {
    const start = (this.ordersPage - 1) * this.ordersPageSize;
    return this.recentOrders.slice(start, start + this.ordersPageSize);
  }
  get totalOrderPages(): number {
    return Math.max(1, Math.ceil(this.recentOrders.length / this.ordersPageSize));
  }
  get orderPageNumbers(): number[] {
    return Array.from({ length: this.totalOrderPages }, (_, i) => i + 1);
  }

  constructor(
    private sellerService: SellerService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userName = this.auth.getUserName();

    this.sellerService.getDashboard().subscribe({
      next: res => {
        if (res.success) this.stats = res.data;
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 404) {
          this.sellerService.registerSeller({
            storeName: this.userName + "'s Store",
            location: '',
            pincode: ''
          }).subscribe({
            next: () => { this.setupBanner = true; this.ngOnInit(); },
            error: () => (this.loading = false)
          });
        } else {
          this.loading = false;
        }
      }
    });

    this.sellerService.getMyOrders().subscribe({
      next: res => { if (res.success) this.recentOrders = res.data; }
    });

    this.sellerService.getMyProducts().subscribe({
      next: res => { if (res.success) this.products = res.data; }
    });
  }

  getGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good morning';
    if (h < 17) return 'Good afternoon';
    return 'Good evening';
  }

  getStatusClass(status: string): string {
    return `status-${(status || 'placed').toLowerCase()}`;
  }

  updateStatus(orderId: number, status: string): void {
    this.sellerService.updateOrderStatus(orderId, status).subscribe({
      next: () => this.ngOnInit()
    });
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalOrderPages) this.ordersPage = page;
  }
}
