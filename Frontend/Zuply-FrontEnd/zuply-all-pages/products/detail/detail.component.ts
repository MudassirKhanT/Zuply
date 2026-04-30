import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { ReviewService, Review, ReviewRequest } from '../../../core/services/review.service';
import { Product } from '../../../core/models';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent implements OnInit {
  product:  Product | null = null;
  loading   = true;
  quantity  = 1;
  addingCart     = false;
  addingWishlist = false;
  toast: { msg: string; type: 'success' | 'error' } | null = null;

  // Reviews
  reviews: Review[] = [];
  avgRating = 0;
  reviewCount = 0;
  reviewRating = 5;
  reviewComment = '';
  submittingReview = false;
  reviewSubmitted = false;

  constructor(
    private route:           ActivatedRoute,
    private router:          Router,
    private productService:  ProductService,
    private cartService:     CartService,
    private wishlistService: WishlistService,
    private auth:            AuthService,
    private reviewService:   ReviewService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getById(id).subscribe({
      next: res => {
        if (res.success) {
          this.product = res.data;
          this.loadReviews(id);
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  loadReviews(productId: number): void {
    this.reviewService.getReviews(productId).subscribe({
      next: res => {
        if (res.success && res.data) {
          this.reviews = res.data.reviews || [];
          this.avgRating = res.data.averageRating || 0;
          this.reviewCount = res.data.count || 0;
        }
      },
      error: () => {}
    });
  }

  submitReview(): void {
    if (!this.product || !this.reviewComment.trim() || this.submittingReview) return;
    this.submittingReview = true;
    const req: ReviewRequest = { rating: this.reviewRating, comment: this.reviewComment.trim() };
    this.reviewService.addReview(this.product.id, req).subscribe({
      next: res => {
        this.submittingReview = false;
        if (res.success) {
          this.reviewSubmitted = true;
          this.reviews = [res.data, ...this.reviews];
          this.reviewCount++;
        }
      },
      error: () => { this.submittingReview = false; }
    });
  }

  isCategory(cat: string): boolean {
    if (!this.product) return false;
    const name = (this.product.categoryName || this.product.category || '').toLowerCase();
    return name.includes(cat.toLowerCase());
  }

  getVariationsArray(): string[] {
    if (!this.product?.variations) return [];
    try { return JSON.parse(this.product.variations); } catch { return this.product.variations.split(',').map(s => s.trim()); }
  }

  starsArray(rating: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }

  increment(): void { this.quantity++; }
  decrement(): void { if (this.quantity > 1) this.quantity--; }

  private requireLogin(): boolean {
    if (!this.auth.isAuthenticated()) {
      const returnUrl = this.router.url;
      this.router.navigate(['/login'], { queryParams: { returnUrl } });
      return false;
    }
    return true;
  }

  addToCart(): void {
    if (!this.product || !this.requireLogin()) return;
    this.addingCart = true;
    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: res => {
        this.addingCart = false;
        if (res.success) {
          this.showToast('Added to cart!', 'success');
          setTimeout(() => this.router.navigate(['/cart']), 900);
        } else {
          this.showToast(res.message || 'Could not add to cart.', 'error');
        }
      },
      error: err => {
        this.addingCart = false;
        this.showToast(err?.error?.message || 'Could not add to cart.', 'error');
      }
    });
  }

  addToWishlist(): void {
    if (!this.product || !this.requireLogin()) return;
    this.addingWishlist = true;
    this.wishlistService.addToWishlist(this.product.id).subscribe({
      next: res => {
        this.addingWishlist = false;
        this.showToast(res.success ? 'Added to wishlist!' : (res.message || 'Could not add to wishlist.'),
                       res.success ? 'success' : 'error');
      },
      error: err => {
        this.addingWishlist = false;
        this.showToast(err?.error?.message || 'Could not add to wishlist.', 'error');
      }
    });
  }

  private showToast(msg: string, type: 'success' | 'error'): void {
    this.toast = { msg, type };
    setTimeout(() => this.toast = null, 3000);
  }
}
