// src/app/pages/admin/sellers/sellers.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-sellers', templateUrl:'./sellers.component.html', styleUrls:['./sellers.component.scss'] })
export class SellersComponent implements OnInit {
  sellers: any[] = [];
  loading = true;
  actionMsg = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadSellers();
  }

  loadSellers(): void {
    this.loading = true;
    this.adminService.getSellers().subscribe({
      next: res => { if (res.success) this.sellers = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  approve(id: number): void {
    this.adminService.approveSeller(id).subscribe({
      next: res => { if (res.success) { this.actionMsg = 'Seller approved.'; this.loadSellers(); } }
    });
  }

  suspend(id: number): void {
    if (!confirm('Suspend this seller?')) return;
    this.adminService.suspendSeller(id).subscribe({
      next: res => { if (res.success) { this.actionMsg = 'Seller suspended.'; this.loadSellers(); } }
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this seller permanently?')) return;
    this.adminService.deleteSeller(id).subscribe({
      next: res => { if (res.success) { this.actionMsg = 'Seller deleted.'; this.loadSellers(); } },
      error: () => this.actionMsg = 'Delete failed.'
    });
  }

  getStatusClass(seller: any): string {
    if (!seller.active) return 'status-suspended';
    if (seller.verificationStatus === 'APPROVED') return 'status-approved';
    return 'status-pending';
  }
}
