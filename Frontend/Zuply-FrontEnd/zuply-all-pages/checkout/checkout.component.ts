// src/app/pages/checkout/checkout.component.ts
declare const Razorpay: any;

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { OrderService } from '../../core/services/order.service';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { PaymentService } from '../../core/services/payment.service';
import { CartResponse, CheckoutPayload, SavedAddress } from '../../core/models';

@Component({ selector:'app-checkout', templateUrl:'./checkout.component.html', styleUrls:['./checkout.component.scss'] })
export class CheckoutComponent implements OnInit {

  cart: CartResponse | null = null;
  loading  = true;
  placing  = false;
  errorMsg = '';

  // Success state
  orderSuccess     = false;
  successOrderId   = 0;
  successTotal     = 0;

  deliveryAddress = '';
  city            = '';
  pincode         = '';
  phone           = '';
  paymentMethod: 'UPI' | 'CARD' | 'COD' = 'COD';

  // touched flags for inline validation
  addressTouched = false;
  cityTouched    = false;
  pincodeTouched = false;
  phoneTouched   = false;

  /** Indian mobile: starts with 6–9, exactly 10 digits */
  get isPhoneValid(): boolean {
    return /^[6-9]\d{9}$/.test(this.phone.trim());
  }

  /** Indian 6-digit pincode */
  get isPincodeValid(): boolean {
    return /^\d{6}$/.test(this.pincode.trim());
  }

  savedAddresses: SavedAddress[] = [];
  selectedAddressId = '';

  paymentOptions = [
    { value: 'COD',  label: 'Cash on Delivery', icon: '💵', desc: 'Pay when your order arrives' },
    { value: 'UPI',  label: 'UPI Payment',       icon: '📱', desc: 'GPay, PhonePe, Paytm etc.' },
    { value: 'CARD', label: 'Card Payment',       icon: '💳', desc: 'Debit or Credit Card' },
  ];

  get successConfig(): { icon: string; title: string; message: string; sub: string } {
    const configs: Record<string, { icon: string; title: string; message: string; sub: string }> = {
      COD:  { icon: '🎉', title: 'Order Placed!',       message: 'Pay ₹ ' + this.successTotal + ' when your order arrives at your door.',   sub: 'Our delivery partner will collect the payment on delivery.' },
      UPI:  { icon: '✅', title: 'Order Confirmed!',    message: 'Your UPI payment of ₹ ' + this.successTotal + ' was successful.',         sub: 'A confirmation will be sent to your registered email.' },
      CARD: { icon: '✅', title: 'Payment Successful!', message: 'Your card payment of ₹ ' + this.successTotal + ' has been confirmed.',    sub: 'A confirmation email will be sent to your registered address.' },
    };
    return configs[this.paymentMethod] || configs['COD'];
  }

  constructor(
    private orderService: OrderService,
    private cartService: CartService,
    private router: Router,
    private auth: AuthService,
    private paymentService: PaymentService
  ) {}

  ngOnInit(): void {
    this.cartService.getCart().subscribe({
      next: res => { if (res.success) this.cart = res.data; this.loading = false; },
      error: () => this.loading = false
    });
    this.loadSavedAddresses();
  }

  private loadSavedAddresses(): void {
    try {
      this.savedAddresses = JSON.parse(localStorage.getItem('zuply_addresses') || '[]');
      const def = this.savedAddresses.find(a => a.isDefault);
      if (def) this.applySavedAddress(def);
    } catch { /* ignore */ }
  }

  selectSavedAddress(addr: SavedAddress): void {
    this.selectedAddressId = addr.id;
    this.applySavedAddress(addr);
  }

  private applySavedAddress(addr: SavedAddress): void {
    this.deliveryAddress   = addr.address;
    this.city              = addr.city;
    this.pincode           = addr.pincode;
    this.phone             = addr.phone;
    this.selectedAddressId = addr.id;
  }

  // ── Entry point called by the "Place Order" button ──────────
  placeOrder(): void {
    // Mark all fields touched so inline errors show
    this.addressTouched = this.cityTouched = this.pincodeTouched = this.phoneTouched = true;

    if (!this.deliveryAddress.trim()) {
      this.errorMsg = 'Please enter your delivery address.'; return;
    }
    if (!this.city.trim()) {
      this.errorMsg = 'Please enter your city.'; return;
    }
    if (!this.isPincodeValid) {
      this.errorMsg = 'Pincode must be a valid 6-digit number.'; return;
    }
    if (!this.isPhoneValid) {
      this.errorMsg = 'Phone must be a valid 10-digit Indian mobile number (starts with 6–9).'; return;
    }
    this.placing = true;
    this.errorMsg = '';

    if (this.paymentMethod === 'COD') {
      // COD — place order directly
      this.submitOrder();
    } else {
      // UPI / CARD — open Razorpay first, then place order on success
      this.initiateRazorpay();
    }
  }

  // ── Build the checkout payload ───────────────────────────────
  private buildPayload(): CheckoutPayload {
    return {
      paymentMethod: this.paymentMethod,
      deliveryAddress: {
        customerName: this.auth?.getUserName?.() || '',
        phone:    this.phone,
        address:  this.deliveryAddress,
        city:     this.city,
        pincode:  this.pincode
      }
    };
  }

  // ── Place the Zuply order in the DB ─────────────────────────
  private submitOrder(): void {
    this.orderService.checkout(this.buildPayload()).subscribe({
      next: res => {
        this.placing = false;
        if (res.success) {
          this.cartService.resetCount();
          this.successTotal   = this.cart?.grandTotal || 0;
          this.successOrderId = res.data?.orderId || 0;
          this.orderSuccess   = true;
        } else {
          this.errorMsg = res.message || 'Order failed. Please try again.';
        }
      },
      error: err => {
        this.placing = false;
        this.errorMsg = err.error?.message || 'Order failed. Please try again.';
      }
    });
  }

  // ── Step 1: Create a Razorpay order and open the popup ──────
  private initiateRazorpay(): void {
    const amount = this.cart?.grandTotal || 0;

    this.paymentService.createOrder(amount).subscribe({
      next: res => {
        if (!res.success) {
          this.placing = false;
          this.errorMsg = res.message || 'Could not initiate payment. Try Cash on Delivery.';
          return;
        }

        const { razorpayOrderId, keyId } = res.data;
        const options = {
          key:         keyId,
          amount:      Math.round(amount * 100),   // paise
          currency:    'INR',
          name:        'Zuply',
          description: 'Order Payment',
          order_id:    razorpayOrderId,
          handler: (response: any) => {
            // Payment succeeded — verify then place the order
            this.verifyAndSubmit(
              razorpayOrderId,
              response.razorpay_payment_id,
              response.razorpay_signature
            );
          },
          prefill: {
            name:    this.auth?.getUserName?.() || '',
            contact: this.phone
          },
          theme: { color: '#0D5C63' },
          modal: {
            ondismiss: () => {
              // User closed the popup without paying
              this.placing = false;
              this.errorMsg = 'Payment cancelled. Please try again.';
            }
          }
        };

        const rzp = new Razorpay(options);
        rzp.on('payment.failed', (response: any) => {
          this.placing = false;
          this.errorMsg = response.error?.description || 'Payment failed. Please try again.';
        });
        rzp.open();
      },
      error: err => {
        this.placing = false;
        // err.error is the ApiResponse body from the backend (400 response)
        this.errorMsg = err.error?.message || err.message || 'Payment initiation failed. Please use Cash on Delivery.';
      }
    });
  }

  // ── Step 2: Verify the Razorpay payment signature ───────────
  private verifyAndSubmit(
    razorpayOrderId: string,
    razorpayPaymentId: string,
    razorpaySignature: string
  ): void {
    this.paymentService.verifyPayment({
      razorpayOrderId,
      razorpayPaymentId,
      razorpaySignature,
      orderId: 0   // no Zuply order yet; backend uses razorpayOrderId for lookup
    }).subscribe({
      next: res => {
        if (res.success) {
          // Signature verified — now create the Zuply order
          this.submitOrder();
        } else {
          this.placing = false;
          this.errorMsg = 'Payment verification failed. Please contact support.';
        }
      },
      error: () => {
        this.placing = false;
        this.errorMsg = 'Payment verification failed. Please contact support.';
      }
    });
  }

  goToOrders(): void   { this.router.navigate(['/orders']); }
  goToProducts(): void { this.router.navigate(['/products']); }
}
