// src/app/pages/cart/cart.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { ApiResponse, CartResponse, CartItem } from '../../core/models';

@Component({ selector:'app-cart', templateUrl:'./cart.component.html', styleUrls:['./cart.component.scss'] })
export class CartComponent implements OnInit {
  cart: CartResponse | null = null;
  loading = true;

  constructor(private cartService: CartService, private router: Router) {}

  ngOnInit(): void {
    this.cartService.getCart().subscribe({
      next: res => { if (res.success) this.cart = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  updateQty(item: CartItem, qty: number): void {
    if (qty < 1) return;
    this.cartService.updateQuantity(item.itemId, qty).subscribe({
      next: () => this.ngOnInit(),
      error: () => alert('Failed to update quantity. Please try again.')
    });
  }

  removeItem(item: CartItem): void {
    this.cartService.removeItem(item.itemId).subscribe({
      next: () => this.ngOnInit(),
      error: () => alert('Failed to remove item. Please try again.')
    });
  }

  checkout(): void { this.router.navigate(['/checkout']); }
}
