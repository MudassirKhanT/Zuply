import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-sellers',
  templateUrl: './sellers.component.html',
  styleUrl: './sellers.component.scss'
})
export class SellersComponent implements OnInit {
  sellers: any[] = [];
  loading = true;
  error = '';
  actionMessage = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadSellers();
  }

  loadSellers(): void {
    this.loading = true;
    this.adminService.getSellers().subscribe({
      next: res => {
        if (res.success) this.sellers = res.data;
        else this.error = res.message;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load sellers.';
        this.loading = false;
      }
    });
  }

  approve(id: number): void {
    this.adminService.approveSeller(id).subscribe({
      next: res => {
        if (res.success) {
          this.actionMessage = 'Seller approved.';
          this.loadSellers();
        }
      },
      error: () => this.actionMessage = 'Action failed.'
    });
  }

  suspend(id: number): void {
    this.adminService.suspendSeller(id).subscribe({
      next: res => {
        if (res.success) {
          this.actionMessage = 'Seller suspended.';
          this.loadSellers();
        }
      },
      error: () => this.actionMessage = 'Action failed.'
    });
  }

  delete(id: number): void {
    if (!confirm('Are you sure you want to delete this seller? This cannot be undone.')) return;
    this.adminService.deleteSeller(id).subscribe({
      next: res => {
        if (res.success) {
          this.actionMessage = 'Seller deleted.';
          this.loadSellers();
        }
      },
      error: () => this.actionMessage = 'Delete failed.'
    });
  }

  getStatusClass(seller: any): string {
    if (!seller.active) return 'status-suspended';
    if (seller.verificationStatus === 'APPROVED') return 'status-approved';
    return 'status-pending';
  }
}
