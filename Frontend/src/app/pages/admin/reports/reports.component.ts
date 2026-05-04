import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.scss'
})
export class ReportsComponent implements OnInit {
  report: any = null;
  loading = true;
  error = '';
  categoryEntries: { name: string; count: number }[] = [];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.adminService.getReports().subscribe({
      next: res => {
        if (res.success) {
          this.report = res.data;
          if (res.data.productsByCategory) {
            this.categoryEntries = Object.entries(res.data.productsByCategory)
              .map(([name, count]) => ({ name, count: count as number }))
              .sort((a, b) => b.count - a.count);
          }
        } else {
          this.error = res.message;
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load reports.';
        this.loading = false;
      }
    });
  }
}
