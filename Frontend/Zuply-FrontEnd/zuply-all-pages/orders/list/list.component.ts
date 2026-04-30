import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { ReviewService, ReviewRequest } from '../../../core/services/review.service';
import { Order } from '../../../core/models';

interface ReviewState {
  open: boolean;
  rating: number;
  comment: string;
  submitting: boolean;
  submitted: boolean;
}

@Component({ selector: 'app-list', templateUrl: './list.component.html', styleUrls: ['./list.component.scss'] })
export class ListComponent implements OnInit {
  orders: Order[] = [];
  filtered: Order[] = [];
  loading  = true;
  activeTab = 'ALL';
  tabs = ['ALL', 'PLACED', 'PROCESSING', 'DELIVERED', 'CANCELLED'];

  reviewStates: { [orderId_productId: string]: ReviewState } = {};

  constructor(
    private orderService: OrderService,
    private reviewService: ReviewService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe({
      next: res => { if (res.success) { this.orders = res.data; this.filter(); } this.loading = false; },
      error: () => this.loading = false
    });
  }

  filter(): void {
    this.filtered = this.activeTab === 'ALL'
      ? this.orders
      : this.orders.filter(o => o.status === this.activeTab);
  }

  setTab(tab: string): void { this.activeTab = tab; this.filter(); }

  viewDetail(order: Order): void { this.router.navigate(['/orders', order.orderId]); }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      PLACED: 'badge-placed', PROCESSING: 'badge-processing',
      DELIVERED: 'badge-delivered', CANCELLED: 'badge-cancelled'
    };
    return 'status-badge ' + (map[status] || 'badge-placed');
  }

  /** Returns 0-3 based on status */
  progressStep(status: string): number {
    const steps: Record<string, number> = { PLACED: 0, PROCESSING: 1, SHIPPED: 2, DELIVERED: 3 };
    return steps[status] ?? 0;
  }

  // ── Inline review ────────────────────────────────────────────
  reviewKey(orderId: number, productId: number): string { return `${orderId}_${productId}`; }

  getReviewState(orderId: number, productId: number): ReviewState {
    const key = this.reviewKey(orderId, productId);
    if (!this.reviewStates[key]) {
      this.reviewStates[key] = { open: false, rating: 5, comment: '', submitting: false, submitted: false };
    }
    return this.reviewStates[key];
  }

  openReview(orderId: number, productId: number): void {
    const state = this.getReviewState(orderId, productId);
    state.open = !state.open;
  }

  setRating(orderId: number, productId: number, rating: number): void {
    this.getReviewState(orderId, productId).rating = rating;
  }

  submitReview(orderId: number, productId: number): void {
    const state = this.getReviewState(orderId, productId);
    if (!state.comment.trim() || state.submitting || state.submitted) return;
    state.submitting = true;

    const req: ReviewRequest = { rating: state.rating, comment: state.comment.trim() };
    this.reviewService.addReview(productId, req).subscribe({
      next: () => { state.submitting = false; state.submitted = true; state.open = false; },
      error: () => { state.submitting = false; }
    });
  }
}
