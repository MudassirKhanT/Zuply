import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product, ReviewsResponse } from '../../../core/models';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent implements OnInit {

  product:          Product | null = null;
  reviewData:       ReviewsResponse | null = null;
  loading           = true;
  qty               = 1;
  isWishlisted      = false;
  showLoginPrompt   = false;
  alreadyReviewed   = false;
  reviewSuccess     = false;
  submittingReview  = false;
  currentUrl        = '';

  reviewForm = { rating: 0, comment: '' };

  get isLoggedIn(): boolean { return this.auth.isLoggedIn(); }
  get isCustomer(): boolean { return this.auth.isCustomer(); }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private reviewService: ReviewService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUrl = this.router.url;
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getById(id).subscribe({
      next: res => {
        this.product = res.success ? res.data : null;
        this.loading = false;
        if (this.product) this.loadReviews(id);
      },
      error: () => this.loading = false
    });
  }

  loadReviews(productId: number): void {
    this.reviewService.getReviews(productId).subscribe({
      next: res => {
        if (res.success) {
          this.reviewData = res.data;
          if (this.isCustomer) {
            const userName = this.auth.getUserName();
            this.alreadyReviewed = res.data.reviews.some(r => r.customerName === userName);
          }
        }
      }
    });
  }

  incQty(): void { if (this.product && this.qty < (this.product.stock || 99)) this.qty++; }
  decQty(): void { if (this.qty > 1) this.qty--; }

  addToCart(): void {
    if (!this.auth.isLoggedIn()) { this.showLoginPrompt = true; return; }
    this.cartService.addToCart(this.product!.id, this.qty).subscribe({
      next: () => this.router.navigate(['/cart'])
    });
  }

  toggleWishlist(): void {
    if (!this.auth.isLoggedIn()) { this.showLoginPrompt = true; return; }
    if (this.isWishlisted) {
      this.wishlistService.removeFromWishlist(this.product!.id).subscribe(() => this.isWishlisted = false);
    } else {
      this.wishlistService.addToWishlist(this.product!.id).subscribe(() => this.isWishlisted = true);
    }
  }

  submitReview(): void {
    if (this.reviewForm.rating === 0 || !this.product) return;
    this.submittingReview = true;
    this.reviewService.addReview(this.product.id, this.reviewForm).subscribe({
      next: res => {
        this.submittingReview = false;
        if (res.success) {
          this.reviewSuccess   = true;
          this.alreadyReviewed = true;
          this.reviewForm      = { rating: 0, comment: '' };
          this.loadReviews(this.product!.id);
        }
      },
      error: () => this.submittingReview = false
    });
  }

  isCategory(name: string): boolean {
    const cat = this.product?.categoryName || this.product?.category || '';
    return cat.toLowerCase() === name.toLowerCase();
  }

  starsArray(rating: number): number[] {
    return Array(Math.round(rating)).fill(0);
  }
  emptyStars(rating: number): number[] {
    return Array(5 - Math.round(rating)).fill(0);
  }
}
