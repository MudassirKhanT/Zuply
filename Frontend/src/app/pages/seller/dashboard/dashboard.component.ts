import { Component, OnInit } from '@angular/core';
import { SellerService } from '../../../core/services/seller.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  stats: any = null;
  recentOrders: any[] = [];
  loading = true;
  error = '';

  constructor(private sellerService: SellerService) {}

  ngOnInit(): void {
    this.sellerService.getDashboard().subscribe({
      next: res => {
        if (res.success) this.stats = res.data;
        else this.error = res.message;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load dashboard.';
        this.loading = false;
      }
    });

    this.sellerService.getMyOrders().subscribe({
      next: res => {
        if (res.success) this.recentOrders = res.data.slice(0, 5);
      }
    });
  }
}
