import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-advertise',
  templateUrl: './advertise.component.html',
  styleUrls: ['./advertise.component.scss']
})
export class AdvertiseComponent {

  benefits = [
    { icon: '🎯', title: 'Targeted Local Reach',    desc: 'Show your products to buyers searching in your pincode and city — no wasted impressions.' },
    { icon: '🤖', title: 'AI-Powered Listings',      desc: 'Upload a photo and let Zuply AI write your title, description, tags and price range instantly.' },
    { icon: '📊', title: 'Real-Time Analytics',      desc: 'Track views, clicks, and orders from your seller dashboard with daily breakdowns.' },
    { icon: '💰', title: 'Affordable Pricing',       desc: 'Start selling for free. Paid promotion options available at flexible budgets from ₹99/month.' },
    { icon: '🚀', title: 'Fast Onboarding',          desc: 'Go from signup to your first listing in under 10 minutes. No technical skills required.' },
    { icon: '🛡️', title: 'Secure Payments',          desc: 'Receive payments directly to your bank account via Razorpay with zero payment disputes.' },
  ];

  steps = [
    { num: '01', title: 'Register as a Seller',   desc: 'Create your free seller account in minutes with just your name, email and store name.' },
    { num: '02', title: 'Upload Your Products',   desc: 'Take a photo of your product and let our Gemini AI generate a professional listing instantly.' },
    { num: '03', title: 'Get Admin Approval',     desc: 'Our team reviews your listing to ensure quality. Approval typically takes less than 24 hours.' },
    { num: '04', title: 'Start Receiving Orders', desc: 'Once approved, your products are live on the marketplace and customers can find and order them.' },
  ];

  plans = [
    {
      name: 'Free',
      price: '₹0',
      period: 'forever',
      highlight: false,
      features: ['Up to 10 product listings', 'AI listing generation', 'Basic seller dashboard', 'Razorpay payouts', 'Email support'],
    },
    {
      name: 'Growth',
      price: '₹299',
      period: 'per month',
      highlight: true,
      features: ['Unlimited product listings', 'Priority placement in search', 'Sponsored product badges', 'Advanced analytics', '24/7 priority support'],
    },
    {
      name: 'Pro',
      price: '₹799',
      period: 'per month',
      highlight: false,
      features: ['Everything in Growth', 'Homepage banner placement', 'Category page featured slot', 'Dedicated account manager', 'Custom promotional campaigns'],
    },
  ];

  constructor(private router: Router) {}

  getStarted(): void {
    this.router.navigate(['/become-a-seller']);
  }
}
