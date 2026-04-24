// src/app/pages/admin/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';
import { AdminStats } from '../../../core/models';

@Component({ selector:'app-dashboard', templateUrl:'./dashboard.component.html', styleUrls:['./dashboard.component.scss'] })
export class DashboardComponent implements OnInit {
  stats: AdminStats | null = null;
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: res => { if (res.success) this.stats = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }
}
