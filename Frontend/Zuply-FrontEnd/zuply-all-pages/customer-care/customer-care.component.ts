import { Component } from '@angular/core';

@Component({
  selector: 'app-customer-care',
  templateUrl: './customer-care.component.html',
  styleUrls: ['./customer-care.component.scss']
})
export class CustomerCareComponent {

  faqs = [
    {
      q: 'How do I track my order?',
      a: 'Go to My Orders from the navigation menu. Click on any order to see its current status and estimated delivery date.',
      open: false
    },
    {
      q: 'How do I cancel or return an order?',
      a: 'You can request a cancellation before the order is marked as "Processing". For returns, contact our support team within 7 days of delivery with your order ID.',
      open: false
    },
    {
      q: 'I was charged but my order was not placed. What do I do?',
      a: 'Payment failures are automatically refunded within 5–7 business days. If you were charged and the order did not appear, please contact us with your transaction ID.',
      open: false
    },
    {
      q: 'How do I become a seller on Zuply?',
      a: 'Click "Become a Seller" in the navigation and complete the seller registration form. Your account will be reviewed and activated within 24 hours.',
      open: false
    },
    {
      q: 'How do I update my delivery address?',
      a: 'Go to Saved Addresses under your account menu. You can add, edit, or set a default address for faster checkout.',
      open: false
    },
    {
      q: 'Is my payment information secure?',
      a: 'Yes. Zuply uses Razorpay, a PCI-DSS compliant payment gateway. We never store your card details on our servers.',
      open: false
    },
  ];

  toggle(faq: any): void {
    faq.open = !faq.open;
  }
}
