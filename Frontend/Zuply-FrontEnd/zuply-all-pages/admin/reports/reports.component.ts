// src/app/pages/admin/reports/reports.component.ts
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({ selector:'app-reports', templateUrl:'./reports.component.html', styleUrls:['./reports.component.scss'] })
export class ReportsComponent implements OnInit {
  reports: any = null;
  loading = true;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getReports().subscribe({
      next: res => { if (res.success) this.reports = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }
}
