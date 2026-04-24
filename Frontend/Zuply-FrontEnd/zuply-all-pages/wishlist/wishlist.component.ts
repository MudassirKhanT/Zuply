// src/app/pages/wishlist/wishlist.component.ts
import { Component, OnInit } from '@angular/core';
import { WishlistService } from '../../core/services/wishlist.service';
import { CartService } from '../../core/services/cart.service';
import { WishlistItem } from '../../core/models';

@Component({ selector:'app-wishlist', templateUrl:'./wishlist.component.html', styleUrls:['./wishlist.component.scss'] })
export class WishlistComponent implements OnInit {
  items: WishlistItem[] = [];
  loading = true;

  constructor(private wishlistService: WishlistService, private cartService: CartService) {}

  ngOnInit(): void {
    this.wishlistService.getWishlist().subscribe({
      next: res => { if (res.success) this.items = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  remove(item: WishlistItem): void {
    this.wishlistService.removeFromWishlist(item.productId).subscribe(() => this.ngOnInit());
  }

  moveToCart(item: WishlistItem): void {
    this.cartService.addToCart(item.productId).subscribe(() => this.remove(item));
  }
}
