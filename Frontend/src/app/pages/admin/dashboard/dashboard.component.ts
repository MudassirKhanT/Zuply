import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  stats: any = null;
  loading = true;
  error = '';

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
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
  }

  navigate(path: string): void {
    this.router.navigate([path]);
  }
}
