// src/app/pages/admin/sellers/sellers.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-sellers', templateUrl:'./sellers.component.html', styleUrls:['./sellers.component.scss'] })
export class SellersComponent implements OnInit {
  sellers: any[] = [];
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getSellers().subscribe({
      next: res => { if (res.success) this.sellers = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  approve(id: number): void {
    this.adminService.approveSeller(id).subscribe(() => this.ngOnInit());
  }

  suspend(id: number): void {
    if (!confirm('Suspend this seller?')) return;
    this.adminService.suspendSeller(id).subscribe(() => this.ngOnInit());
  }

  reject(id: number): void {
    if (!confirm('Reject this seller registration?')) return;
    this.adminService.rejectSeller(id).subscribe(() => this.ngOnInit());
  }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }
}
