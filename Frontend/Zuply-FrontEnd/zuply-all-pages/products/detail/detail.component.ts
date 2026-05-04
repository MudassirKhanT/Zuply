// src/app/pages/products/detail/detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product, Review } from '../../../core/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent implements OnInit {
  product: Product | null = null;
  loading        = true;
  addingToCart   = false;
  quantity       = 1;
  actionMsg      = '';
  actionMsgType: 'success' | 'error' = 'success';
  selectedImage  = '';
  private msgTimer: any;

  // Reviews
  reviews: Review[]    = [];
  reviewsLoading       = false;
  newRating            = 0;
  newComment           = '';
  hoverRating          = 0;
  submittingReview     = false;
  reviewMsg            = '';
  reviewMsgType: 'success' | 'error' = 'success';
  private reviewMsgTimer: any;

  get isCustomer(): boolean { return this.authService.getRole() === 'CUSTOMER'; }
  get isLoggedIn(): boolean { return this.authService.isAuthenticated(); }

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private reviewService: ReviewService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getById(id).subscribe({
      next: res => {
        if (res.success) {
          this.product = res.data;
          this.selectedImage = res.data?.imageUrl || '';
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
    this.loadReviews(id);
  }

  loadReviews(productId: number): void {
    this.reviewsLoading = true;
    this.reviewService.getReviews(productId).subscribe({
      next: res => { if (res.success) this.reviews = res.data; this.reviewsLoading = false; },
      error: () => (this.reviewsLoading = false)
    });
  }

  submitReview(): void {
    if (!this.product || this.newRating === 0) {
      this.showReviewMsg('Please select a star rating.', 'error'); return;
    }
    this.submittingReview = true;
    this.reviewService.submitReview(this.product.id, { rating: this.newRating, comment: this.newComment }).subscribe({
      next: res => {
        this.submittingReview = false;
        if (res.success) {
          this.showReviewMsg('Thank you for your review!', 'success');
          this.newRating = 0; this.newComment = '';
          this.loadReviews(this.product!.id);
        } else {
          this.showReviewMsg(res.message || 'Could not submit review.', 'error');
        }
      },
      error: (err: HttpErrorResponse) => {
        this.submittingReview = false;
        this.showReviewMsg(err.error?.message || 'Could not submit review.', 'error');
      }
    });
  }

  starArray(n: number): number[] { return Array.from({ length: n }, (_, i) => i + 1); }

  increment(): void { this.quantity++; }
  decrement(): void { if (this.quantity > 1) this.quantity--; }

  addToCart(): void {
    if (!this.product || this.addingToCart) return;
    this.addingToCart = true;
    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: res => {
        this.addingToCart = false;
        if (res.success) this.router.navigate(['/cart']);
        else this.showMsg(res.message || 'Could not add to cart.', 'error');
      },
      error: (err: HttpErrorResponse) => {
        this.addingToCart = false;
        this.showMsg(err.error?.message || 'Could not add to cart. Please try again.', 'error');
      }
    });
  }

  addToWishlist(): void {
    if (!this.product) return;
    this.wishlistService.addToWishlist(this.product.id).subscribe({
      next: res => {
        if (res.success) this.showMsg('Added to wishlist!', 'success');
        else this.showMsg(res.message || 'Could not add to wishlist.', 'error');
      },
      error: () => this.showMsg('Could not add to wishlist. Please try again.', 'error')
    });
  }

  private showMsg(msg: string, type: 'success' | 'error'): void {
    this.actionMsg = msg; this.actionMsgType = type;
    clearTimeout(this.msgTimer);
    this.msgTimer = setTimeout(() => this.actionMsg = '', 3000);
  }

  private showReviewMsg(msg: string, type: 'success' | 'error'): void {
    this.reviewMsg = msg; this.reviewMsgType = type;
    clearTimeout(this.reviewMsgTimer);
    this.reviewMsgTimer = setTimeout(() => this.reviewMsg = '', 4000);
  }
}
