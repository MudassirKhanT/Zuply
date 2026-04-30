import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-become-a-seller',
  templateUrl: './become-a-seller.component.html',
  styleUrls: ['./become-a-seller.component.scss']
})
export class BecomeASellerComponent {

  categories = [
    { icon: '⚡', name: 'Electronics',           desc: 'Phones, laptops, gadgets' },
    { icon: '👗', name: 'Clothing',              desc: 'Apparel for all ages' },
    { icon: '🛒', name: 'Grocery',               desc: 'Daily essentials & staples' },
    { icon: '🍔', name: 'Food & Beverage',        desc: 'Packaged food & drinks' },
    { icon: '🏠', name: 'Home & Kitchen',         desc: 'Furniture & cookware' },
    { icon: '💄', name: 'Beauty & Personal Care', desc: 'Skincare & cosmetics' },
    { icon: '💊', name: 'Health & Wellness',      desc: 'Supplements & fitness' },
    { icon: '🌾', name: 'Agriculture',            desc: 'Seeds, tools & produce' },
    { icon: '👟', name: 'Fashion & Footwear',     desc: 'Shoes & accessories' },
  ];

  listingSteps = [
    { num: '01', icon: '📷', title: 'Upload a Photo',   desc: 'Take a clear product photo and upload it.' },
    { num: '02', icon: '🤖', title: 'AI Does the Work', desc: 'AI auto-fills title, description, category & price range.' },
    { num: '03', icon: '✏️', title: 'Review & Edit',    desc: 'Check the details, adjust anything you like.' },
    { num: '04', icon: '🚀', title: 'Publish',          desc: 'Hit publish — your product goes live instantly.' },
  ];

  paymentPoints = [
    { icon: '📱', title: 'UPI Payments',  desc: 'Receive money directly to your UPI ID.' },
    { icon: '🏦', title: 'Bank Transfer', desc: 'Earnings settled weekly to your bank account.' },
    { icon: '🔒', title: 'Secure & Fast', desc: 'Transactions via Razorpay — PCI-DSS compliant.' },
    { icon: '📊', title: 'Track Earnings', desc: 'View sales, pending payouts from your dashboard.' },
  ];

  constructor(private router: Router) {}

  goToRegister(): void {
    this.router.navigate(['/register'], { queryParams: { role: 'SELLER' } });
  }
}
