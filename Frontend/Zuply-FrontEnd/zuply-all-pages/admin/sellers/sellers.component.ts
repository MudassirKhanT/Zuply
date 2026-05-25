// src/app/pages/admin/sellers/sellers.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-sellers', templateUrl:'./sellers.component.html', styleUrls:['./sellers.component.scss'] })
export class SellersComponent implements OnInit {
  allSellers: any[] = [];
  loading     = true;
  activeFilter: 'all' | 'approved' | 'pending' | 'suspended' = 'all';
  searchQuery = '';

  readonly filters: { key: string; label: string; icon: string }[] = [
    { key: 'all',       label: 'All Sellers',      icon: '👥' },
    { key: 'approved',  label: 'Approved',          icon: '✅' },
    { key: 'pending',   label: 'Pending Approval',  icon: '⏳' },
    { key: 'suspended', label: 'Suspended',         icon: '🚫' },
  ];

  get sellers(): any[] {
    const q = this.searchQuery.trim().toLowerCase();
    let list: any[];
    switch (this.activeFilter) {
      case 'approved':  list = this.allSellers.filter(s => s.verificationStatus === 'APPROVED' && s.active); break;
      case 'pending':   list = this.allSellers.filter(s => s.verificationStatus === 'PENDING');  break;
      case 'suspended': list = this.allSellers.filter(s => s.verificationStatus === 'SUSPENDED' || (s.verificationStatus === 'APPROVED' && !s.active)); break;
      default:          list = this.allSellers;
    }
    return q ? list.filter(s => (s.name || '').toLowerCase().includes(q) || (s.storeName || '').toLowerCase().includes(q) || (s.email || '').toLowerCase().includes(q)) : list;
  }

  get filterCounts(): Record<string, number> {
    return {
      all:       this.allSellers.length,
      approved:  this.allSellers.filter(s => s.verificationStatus === 'APPROVED' && s.active).length,
      pending:   this.allSellers.filter(s => s.verificationStatus === 'PENDING').length,
      suspended: this.allSellers.filter(s => s.verificationStatus === 'SUSPENDED' || (s.verificationStatus === 'APPROVED' && !s.active)).length,
    };
  }

  constructor(
    private adminService: AdminService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Read filter from query param (e.g. ?filter=approved)
    this.route.queryParams.subscribe(params => {
      const f = params['filter'];
      if (f && ['all','approved','pending','suspended'].includes(f)) {
        this.activeFilter = f as any;
      }
    });

    this.adminService.getSellers().subscribe({
      next: res => { if (res.success) this.allSellers = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  setFilter(key: string): void {
    this.activeFilter = key as any;
    this.router.navigate([], { queryParams: { filter: key }, queryParamsHandling: 'merge', replaceUrl: true });
  }

  approve(id: number): void {
    this.adminService.approveSeller(id).subscribe({
      next: () => this.ngOnInit(),
      error: err => alert(err?.error?.message || 'Failed to approve seller.')
    });
  }

  suspend(id: number): void {
    if (!confirm('Suspend this seller?')) return;
    this.adminService.suspendSeller(id).subscribe({
      next: () => this.ngOnInit(),
      error: err => alert(err?.error?.message || 'Failed to suspend seller.')
    });
  }

  reject(id: number): void {
    if (!confirm('Reject this seller registration?')) return;
    this.adminService.rejectSeller(id).subscribe({
      next: () => this.ngOnInit(),
      error: err => alert(err?.error?.message || 'Failed to reject seller.')
    });
  }

  getStatusClass(status: string): string { return `status-${(status || 'pending').toLowerCase()}`; }
}
