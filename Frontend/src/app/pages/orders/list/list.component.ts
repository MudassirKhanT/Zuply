import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { Order, OrderItem } from '../../../core/models';

interface ReviewState {
  open: boolean;
  rating: number;
  comment: string;
  submitting: boolean;
  done: boolean;
}

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  orders: Order[] = [];
  loading         = true;
  errorMsg        = '';

  // productId → review state
  reviewStates: Record<number, ReviewState> = {};

  constructor(
    private orderService: OrderService,
    private reviewService: ReviewService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe({
      next: res => {
        if (res.success) this.orders = res.data;
        else this.errorMsg = res.message || 'Could not load orders.';
        this.loading = false;
      },
      error: () => {
        this.errorMsg = 'Failed to load orders. Please try again.';
        this.loading = false;
      }
    });
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      PLACED: 'status-placed', PROCESSING: 'status-processing', DELIVERED: 'status-delivered'
    };
    return map[status] || 'status-placed';
  }

  statusIcon(status: string): string {
    const map: Record<string, string> = { PLACED: '📋', PROCESSING: '🔄', DELIVERED: '✅' };
    return map[status] || '📋';
  }

  progressStep(status: string): number {
    const steps: Record<string, number> = { PLACED: 1, PROCESSING: 2, DELIVERED: 3 };
    return steps[status] || 1;
  }

  // ── Review helpers ────────────────────────────────────
  openReview(productId: number): void {
    this.reviewStates[productId] = { open: true, rating: 0, comment: '', submitting: false, done: false };
  }

  closeReview(productId: number): void {
    delete this.reviewStates[productId];
  }

  reviewState(productId: number): ReviewState | undefined {
    return this.reviewStates[productId];
  }

  setRating(productId: number, r: number): void {
    if (this.reviewStates[productId]) this.reviewStates[productId].rating = r;
  }

  submitReview(productId: number): void {
    const state = this.reviewStates[productId];
    if (!state || state.rating === 0) return;
    state.submitting = true;
    this.reviewService.addReview(productId, { rating: state.rating, comment: state.comment }).subscribe({
      next: res => {
        state.submitting = false;
        if (res.success) state.done = true;
      },
      error: () => { state.submitting = false; }
    });
  }

  goToProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
  }
}
