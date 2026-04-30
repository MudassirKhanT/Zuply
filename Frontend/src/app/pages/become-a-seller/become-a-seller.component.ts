import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-become-a-seller',
  templateUrl: './become-a-seller.component.html',
  styleUrls: ['./become-a-seller.component.scss']
})
export class BecomeASellerComponent {

  categories = [
    { icon: '⚡', name: 'Electronics',             desc: 'Phones, laptops, gadgets & accessories' },
    { icon: '👗', name: 'Clothing',                desc: 'Apparel for men, women & kids' },
    { icon: '🛒', name: 'Grocery',                 desc: 'Daily essentials & staples' },
    { icon: '🍔', name: 'Food & Beverage',          desc: 'Packaged food, drinks & snacks' },
    { icon: '🏠', name: 'Home & Kitchen',           desc: 'Furniture, cookware & decor' },
    { icon: '💄', name: 'Beauty & Personal Care',   desc: 'Skincare, haircare & cosmetics' },
    { icon: '💊', name: 'Health & Wellness',        desc: 'Supplements, fitness & medical' },
    { icon: '🌾', name: 'Agriculture',              desc: 'Seeds, tools & farm produce' },
    { icon: '👟', name: 'Fashion & Footwear',       desc: 'Shoes, bags & fashion accessories' },
  ];

  listingSteps = [
    { num: '01', icon: '📷', title: 'Upload a Photo',   desc: 'Take a clear product photo and upload it from your phone or computer.' },
    { num: '02', icon: '🤖', title: 'AI Does the Work', desc: 'Our AI reads your image and auto-fills title, description, category, and price range.' },
    { num: '03', icon: '✏️', title: 'Review & Edit',    desc: 'Check the AI-generated details, adjust anything, and add your final price.' },
    { num: '04', icon: '🚀', title: 'Publish',          desc: 'Hit publish — your product goes live on the Zuply marketplace instantly.' },
  ];

  paymentPoints = [
    { icon: '📱', title: 'UPI Payments',     desc: 'Receive money directly to your UPI ID — GPay, PhonePe, Paytm & more.' },
    { icon: '🏦', title: 'Bank Transfer',    desc: 'Earnings are settled to your registered bank account weekly.' },
    { icon: '🔒', title: 'Secure & Fast',    desc: 'All transactions go through Razorpay — PCI-DSS compliant and encrypted.' },
    { icon: '📊', title: 'Track Earnings',   desc: 'View your sales, pending payouts, and transaction history from your seller dashboard.' },
  ];

  constructor(private router: Router) {}

  goToRegister(): void {
    this.router.navigate(['/register'], { queryParams: { role: 'SELLER' } });
  }
}
