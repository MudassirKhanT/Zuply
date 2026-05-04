import { Component, OnInit } from '@angular/core';
import { CartService } from '../../core/services/cart.service';
import { CartItem } from '../../core/models';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss']
})
export class CartComponent implements OnInit {

  items:      CartItem[] = [];
  grandTotal  = 0;
  loading     = true;

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.items      = res.data.items || [];
          this.grandTotal = res.data.grandTotal || 0;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  updateQty(item: CartItem, newQty: number): void {
    if (newQty < 1) return;
    this.cartService.updateQuantity(item.id, newQty).subscribe(() => this.loadCart());
  }

  removeItem(itemId: number): void {
    this.cartService.removeItem(itemId).subscribe(() => this.loadCart());
  }
}
