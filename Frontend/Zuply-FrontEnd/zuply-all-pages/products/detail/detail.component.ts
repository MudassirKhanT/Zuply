// src/app/pages/products/detail/detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { Product } from '../../../core/models';

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent implements OnInit {
  product: Product | null = null;
  loading  = true;
  quantity = 1;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getById(id).subscribe({
      next: res => { if (res.success) this.product = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  increment(): void { this.quantity++; }
  decrement(): void { if (this.quantity > 1) this.quantity--; }

  addToCart(): void {
    if (!this.product) return;
    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: res => { if (res.success) this.router.navigate(['/cart']); }
    });
  }

  addToWishlist(): void {
    if (!this.product) return;
    this.wishlistService.addToWishlist(this.product.id).subscribe();
  }
}
