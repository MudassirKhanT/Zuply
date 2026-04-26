import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';
import { PaymentService } from '../../core/services/payment.service';
import { AuthService } from '../../core/services/auth.service';
import { CartItem, DeliveryAddress, CheckoutRequest } from '../../core/models';

declare const Razorpay: any;

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss']
})
export class CheckoutComponent implements OnInit {

  cartItems:   CartItem[] = [];
  grandTotal   = 0;
  loadingCart  = true;
  placing      = false;
  orderSuccess = false;
  placedOrderId: number | null = null;
  errorMsg     = '';
  paymentMethod: 'COD' | 'UPI' | 'CARD' = 'COD';

  address: DeliveryAddress = {
    customerName: '',
    phone:        '',
    address:      '',
    city:         '',
    pincode:      ''
  };

  constructor(
    private cartService:    CartService,
    private orderService:   OrderService,
    private paymentService: PaymentService,
    private auth:           AuthService,
    private router:         Router
  ) {}

  ngOnInit(): void {
    const user = this.auth.getCurrentUser();
    if (user) {
      this.address.customerName = user.name || '';
    }
    this.cartService.getCart().subscribe({
      next: res => {
        if (res.success && res.data) {
          this.cartItems  = res.data.items || [];
          this.grandTotal = res.data.grandTotal || 0;
        }
        this.loadingCart = false;
      },
      error: () => this.loadingCart = false
    });
  }

  private validateAddress(): boolean {
    const a = this.address;
    if (!a.customerName.trim() || !a.phone.trim() || !a.address.trim() || !a.city.trim() || !a.pincode.trim()) {
      this.errorMsg = 'Please fill in all delivery address fields.';
      return false;
    }
    if (a.pincode.length !== 6) {
      this.errorMsg = 'Pincode must be 6 digits.';
      return false;
    }
    return true;
  }

  placeOrder(): void {
    if (!this.validateAddress()) return;
    this.errorMsg = '';

    if (this.paymentMethod === 'COD') {
      this.submitOrder();
    } else {
      this.initiateRazorpay();
    }
  }

  private submitOrder(razorpayPaymentId?: string): void {
    this.placing = true;
    const req: CheckoutRequest = {
      deliveryAddress: this.address,
      paymentMethod:   this.paymentMethod
    };
    this.orderService.checkout(req).subscribe({
      next: res => {
        this.placing = false;
        if (res.success) {
          this.placedOrderId = res.data.orderId;
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
    this.placing = true;
    this.paymentService.createOrder({ amount: this.grandTotal, orderId: 0 }).subscribe({
      next: res => {
        this.placing = false;
        if (!res.success) { this.errorMsg = 'Could not initiate payment.'; return; }
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
            name:    this.address.customerName,
            email:   user?.email || '',
            contact: this.address.phone
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
        this.errorMsg = err?.error?.message || 'Payment initiation failed.';
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
          this.submitOrder(razorpayResponse.razorpay_payment_id);
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
