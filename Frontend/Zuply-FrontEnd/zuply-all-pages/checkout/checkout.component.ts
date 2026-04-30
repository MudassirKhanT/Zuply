import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { PaymentService } from '../../core/services/payment.service';
import { CartResponse, CheckoutPayload } from '../../core/models';

declare const Razorpay: any;

@Component({ selector: 'app-checkout', templateUrl: './checkout.component.html', styleUrls: ['./checkout.component.scss'] })
export class CheckoutComponent implements OnInit {

  cart: CartResponse | null = null;
  loading  = true;
  placing  = false;
  errorMsg = '';
  orderSuccess  = false;
  placedOrderId: number | null = null;

  deliveryAddress = '';
  city            = '';
  pincode         = '';
  phone           = '';
  paymentMethod: 'UPI' | 'CARD' | 'COD' = 'COD';

  paymentOptions = [
    { value: 'COD',  label: 'Cash on Delivery',    icon: '💵' },
    { value: 'UPI',  label: 'UPI Payment',          icon: '📱' },
    { value: 'CARD', label: 'Credit / Debit Card',  icon: '💳' },
  ];

  constructor(
    private orderService:   OrderService,
    private cartService:    CartService,
    private paymentService: PaymentService,
    private router:         Router,
    private auth:           AuthService
  ) {}

  ngOnInit(): void {
    this.cartService.getCart().subscribe({
      next: res => { if (res.success) this.cart = res.data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  private get deliveryPayload() {
    return {
      customerName: this.auth.getUserName() || '',
      phone:        this.phone,
      address:      this.deliveryAddress,
      city:         this.city,
      pincode:      this.pincode
    };
  }

  private validateForm(): boolean {
    if (!this.deliveryAddress.trim() || !this.city.trim() || !this.pincode.trim() || !this.phone.trim()) {
      this.errorMsg = 'Please fill in all delivery details.';
      return false;
    }
    if (this.pincode.length !== 6) {
      this.errorMsg = 'Pincode must be 6 digits.';
      return false;
    }
    return true;
  }

  placeOrder(): void {
    if (!this.validateForm()) return;
    this.errorMsg = '';

    if (this.paymentMethod === 'COD') {
      this.submitOrder();
    } else {
      this.initiateRazorpay();
    }
  }

  private submitOrder(): void {
    this.placing = true;
    const payload: CheckoutPayload = {
      paymentMethod:   this.paymentMethod,
      deliveryAddress: this.deliveryPayload
    };
    this.orderService.checkout(payload).subscribe({
      next: res => {
        this.placing = false;
        if (res.success) {
          this.placedOrderId = res.data?.orderId || null;
          this.orderSuccess  = true;
          this.cartService.resetCount();
        } else {
          this.errorMsg = res.message || 'Order failed. Please try again.';
        }
      },
      error: err => {
        this.placing  = false;
        this.errorMsg = err?.error?.message || 'Order failed. Please try again.';
      }
    });
  }

  private initiateRazorpay(): void {
    if (!this.cart) return;
    this.placing = true;

    this.paymentService.createOrder(this.cart.grandTotal).subscribe({
      next: res => {
        this.placing = false;
        if (!res.success) {
          this.errorMsg = res.message
            || 'Online payment is currently unavailable. Please use Cash on Delivery.';
          return;
        }

        const data = res.data;
        const user = this.auth.getCurrentUser();

        const options = {
          key:         data.keyId,
          amount:      data.amount * 100,
          currency:    data.currency || 'INR',
          name:        'Zuply',
          description: 'Order Payment',
          order_id:    data.razorpayOrderId,
          prefill: {
            name:    this.auth.getUserName(),
            email:   user?.email || '',
            contact: this.phone
          },
          theme: { color: '#0D5C63' },
          handler: (response: any) => {
            this.verifyAndSubmit(response);
          },
          modal: {
            ondismiss: () => { this.placing = false; }
          }
        };

        const rzp = new Razorpay(options);
        rzp.open();
      },
      error: err => {
        this.placing  = false;
        const detail = err?.error?.message || err?.message || '';
        this.errorMsg = detail
          ? `Payment failed: ${detail}`
          : 'Online payment is currently unavailable. Please switch to Cash on Delivery.';
      }
    });
  }

  private verifyAndSubmit(razorpayResponse: any): void {
    this.placing = true;
    this.paymentService.verifyPayment({
      razorpayOrderId:   razorpayResponse.razorpay_order_id,
      razorpayPaymentId: razorpayResponse.razorpay_payment_id,
      razorpaySignature: razorpayResponse.razorpay_signature
    }).subscribe({
      next: verifyRes => {
        if (verifyRes.success) {
          this.submitOrder();
        } else {
          this.placing  = false;
          this.errorMsg = 'Payment verification failed. Please contact support.';
        }
      },
      error: () => {
        this.placing  = false;
        this.errorMsg = 'Payment verification failed.';
      }
    });
  }
}
