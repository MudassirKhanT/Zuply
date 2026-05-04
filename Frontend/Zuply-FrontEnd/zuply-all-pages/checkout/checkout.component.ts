// src/app/pages/checkout/checkout.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { CartResponse, CheckoutPayload } from '../../core/models';

@Component({ selector:'app-checkout', templateUrl:'./checkout.component.html', styleUrls:['./checkout.component.scss'] })
export class CheckoutComponent implements OnInit {

  cart: CartResponse | null = null;
  loading  = true;
  placing  = false;
  errorMsg = '';

  deliveryAddress = '';
  city            = '';
  pincode         = '';
  phone           = '';
  paymentMethod: 'UPI' | 'CARD' | 'COD' = 'COD';

  paymentOptions = [
    { value: 'COD',  label: 'Cash on Delivery', icon: '💵' },
    { value: 'UPI',  label: 'UPI Payment',       icon: '📱' },
    { value: 'CARD', label: 'Card Payment',       icon: '💳' },
  ];

  constructor(
    private orderService: OrderService,
    private cartService: CartService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.cartService.getCart().subscribe({
      next: res => { if (res.success) this.cart = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  placeOrder(): void {
    if (!this.deliveryAddress || !this.city || !this.pincode || !this.phone) {
      this.errorMsg = 'Please fill all delivery details.'; return;
    }
    this.placing = true; this.errorMsg = '';
    const payload: CheckoutPayload = {
      paymentMethod: this.paymentMethod,
      deliveryAddress: {
        customerName: this.auth?.getUserName?.() || '',
        phone: this.phone,
        address: this.deliveryAddress,
        city: this.city,
        pincode: this.pincode
      }
    };
    this.orderService.checkout(payload).subscribe({
      next: res => {
        this.placing = false;
        if (res.success) { this.cartService.resetCount(); this.router.navigate(['/orders']); }
        else this.errorMsg = res.message || 'Order failed.';
      },
      error: err => { this.placing = false; this.errorMsg = err.error?.message || 'Order failed.'; }
    });
  }
}
