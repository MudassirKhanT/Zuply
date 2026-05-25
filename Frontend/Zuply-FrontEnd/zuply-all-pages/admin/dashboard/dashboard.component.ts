import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AdminService } from '../../../core/services/admin.service';
import { AdminStats } from '../../../core/models';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  stats: AdminStats | null = null;
  allSellers: any[] = [];
  allProducts: any[] = [];
  loading         = true;
  sellersLoading  = true;
  productsLoading = true;

  sellersPage  = 1;
  productsPage = 1;
  pageSize     = 6;

  get pendingSellers(): any[] { return this.allSellers.filter(s => s.verificationStatus === 'PENDING'); }
  // PENDING  = manual products awaiting admin review
  // PUBLISHED = AI/listing products submitted by seller, awaiting admin approval
  get pendingProducts(): any[] { return this.allProducts.filter(p => p.status === 'PENDING' || p.status === 'PUBLISHED'); }
  get approvedSellers(): number { return this.allSellers.filter(s => s.verificationStatus === 'APPROVED').length; }

  get pagedPendingSellers(): any[] {
    const s = (this.sellersPage - 1) * this.pageSize;
    return this.pendingSellers.slice(s, s + this.pageSize);
  }
  get totalSellerPages(): number { return Math.max(1, Math.ceil(this.pendingSellers.length / this.pageSize)); }
  get sellerPageNumbers(): number[] { return Array.from({ length: this.totalSellerPages }, (_, i) => i + 1); }

  get pagedPendingProducts(): any[] {
    const s = (this.productsPage - 1) * this.pageSize;
    return this.pendingProducts.slice(s, s + this.pageSize);
  }
  get totalProductPages(): number { return Math.max(1, Math.ceil(this.pendingProducts.length / this.pageSize)); }
  get productPageNumbers(): number[] { return Array.from({ length: this.totalProductPages }, (_, i) => i + 1); }

  constructor(private adminService: AdminService, private router: Router) {}

  // ── Stat card navigation ────────────────────────────────────────────────────
  goToApprovedSellers(): void  { this.router.navigate(['/admin/sellers'],  { queryParams: { filter: 'approved' } }); }
  goToApprovedProducts(): void { this.router.navigate(['/admin/products'], { queryParams: { filter: 'approved' } }); }
  goToOrders(): void           { this.router.navigate(['/admin/orders']); }
  scrollToPending(): void {
    // Scroll to sellers section first, fall back to products section
    const el = document.getElementById('pending-section')
            || document.getElementById('pending-products-section');
    if (el) { el.scrollIntoView({ behavior: 'smooth', block: 'start' }); }
  }

  ngOnInit(): void {
    this.adminService.getDashboard().subscribe({
      next: res => { if (res.success) this.stats = res.data; this.loading = false; },
      error: () => (this.loading = false)
    });
    this.adminService.getSellers().subscribe({
      next: res => { if (res.success) this.allSellers = res.data; this.sellersLoading = false; },
      error: () => (this.sellersLoading = false)
    });
    this.adminService.getProducts().subscribe({
      next: res => { if (res.success) this.allProducts = res.data; this.productsLoading = false; },
      error: () => (this.productsLoading = false)
    });
  }

  approveSeller(id: number): void {
    this.adminService.approveSeller(id).subscribe({
      next: () => {
        const s = this.allSellers.find(s => s.id === id);
        if (s) { s.verificationStatus = 'APPROVED'; this.sellersPage = 1; }
      }
    });
  }

  rejectSeller(id: number): void {
    if (!confirm('Reject this seller registration?')) return;
    this.adminService.rejectSeller(id).subscribe({
      next: () => {
        this.allSellers = this.allSellers.filter(s => s.id !== id);
        this.sellersPage = 1;
      },
      error: err => alert('Failed to reject: ' + (err.error?.message || 'Unknown error'))
    });
  }

  approveProduct(id: number, source: string = 'LISTING'): void {
    this.adminService.approveProduct(id, source).subscribe({
      next: () => {
        // Replace array reference so Angular change detection picks it up immediately.
        // Match by both id AND source — LISTING and MANUAL products can share the same id.
        this.allProducts = this.allProducts.filter(p => !(p.id === id && p.source === source));
        this.productsPage = 1;
      },
      error: err => {
        alert('Failed to approve: ' + (err.error?.message || err.message || 'Unknown error'));
      }
    });
  }

  rejectProduct(id: number, source: string = 'LISTING'): void {
    if (!confirm('Reject this product?')) return;
    this.adminService.rejectProduct(id, source).subscribe({
      next: () => {
        this.allProducts = this.allProducts.filter(p => !(p.id === id && p.source === source));
        this.productsPage = 1;
      },
      error: err => {
        alert('Failed to reject: ' + (err.error?.message || err.message || 'Unknown error'));
      }
    });
  }

  getSellerStatusClass(status: string): string { return `badge-${(status || 'pending').toLowerCase()}`; }
  getProductStatusClass(status: string): string { return `badge-${(status || 'pending').toLowerCase()}`; }

  goToSellerPage(p: number): void  { if (p >= 1 && p <= this.totalSellerPages)  this.sellersPage  = p; }
  goToProductPage(p: number): void { if (p >= 1 && p <= this.totalProductPages) this.productsPage = p; }

  // ── Create Admin ────────────────────────────────────────────
  showCreateAdmin   = false;
  adminName         = '';
  adminEmail        = '';
  adminPassword     = '';
  adminPhone        = '';
  creatingAdmin     = false;
  createAdminMsg    = '';
  createAdminError  = false;
  private adminMsgTimer: any;

  toggleCreateAdmin(): void { this.showCreateAdmin = !this.showCreateAdmin; }

  createAdmin(): void {
    if (!this.adminName || !this.adminEmail || !this.adminPassword) {
      this.createAdminMsg = 'Name, email and password are required.';
      this.createAdminError = true; return;
    }
    this.creatingAdmin = true; this.createAdminMsg = '';
    this.adminService.createAdmin({
      name: this.adminName, email: this.adminEmail,
      password: this.adminPassword, phone: this.adminPhone
    }).subscribe({
      next: res => {
        this.creatingAdmin = false;
        if (res.success) {
          this.createAdminMsg = 'Admin account created successfully.';
          this.createAdminError = false;
          this.adminName = ''; this.adminEmail = ''; this.adminPassword = ''; this.adminPhone = '';
          this.showCreateAdmin = false;
        } else {
          this.createAdminMsg = res.message || 'Failed to create admin.';
          this.createAdminError = true;
        }
        clearTimeout(this.adminMsgTimer);
        this.adminMsgTimer = setTimeout(() => this.createAdminMsg = '', 4000);
      },
      error: err => {
        this.creatingAdmin = false;
        this.createAdminMsg = err.error?.message || 'Failed to create admin.';
        this.createAdminError = true;
        clearTimeout(this.adminMsgTimer);
        this.adminMsgTimer = setTimeout(() => this.createAdminMsg = '', 4000);
      }
    });
  }
}
