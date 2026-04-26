import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { WishlistService } from '../../core/services/wishlist.service';
import { CartService } from '../../core/services/cart.service';
import { WishlistItem } from '../../core/models';

@Component({
  selector: 'app-wishlist',
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.scss']
})
export class WishlistComponent implements OnInit {

  items:   WishlistItem[] = [];
  loading  = true;
  movingId: number | null = null;

  constructor(
    private wishlistService: WishlistService,
    private cartService:     CartService,
    private router:          Router
  ) {}

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist(): void {
    this.loading = true;
    this.wishlistService.getWishlist().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.items = res.data;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  remove(productId: number): void {
    this.wishlistService.removeFromWishlist(productId).subscribe(() => {
      this.items = this.items.filter(i => i.productId !== productId);
    });
  }

  moveToCart(item: WishlistItem): void {
    this.movingId = item.productId;
    this.cartService.addToCart(item.productId, 1).subscribe({
      next: () => {
        this.wishlistService.removeFromWishlist(item.productId).subscribe(() => {
          this.items = this.items.filter(i => i.productId !== item.productId);
          this.movingId = null;
        });
      },
      error: () => { this.movingId = null; }
    });
  }

  goToProduct(productId: number): void {
    this.router.navigate(['/products', productId]);
  }
}
