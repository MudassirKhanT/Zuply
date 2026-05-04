import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit, OnDestroy {
  isLoggedIn = false;

  counters = [
    { label: 'Local Sellers',    target: 500,   value: 0, suffix: '+' },
    { label: 'Products Listed',  target: 10000, value: 0, suffix: '+' },
    { label: 'Happy Customers',  target: 25000, value: 0, suffix: '+' },
    { label: 'Cities Covered',   target: 50,    value: 0, suffix: '+' }
  ];

  features = [
    { icon: '🏪', title: 'Local First',       desc: 'Discover authentic products from sellers right in your neighbourhood.' },
    { icon: '🤖', title: 'AI-Powered',        desc: 'Smart AI generates polished listings so sellers put their best foot forward.' },
    { icon: '⚡', title: 'Fast Delivery',     desc: 'Local sellers mean shorter distances and quicker delivery to your door.' },
    { icon: '🔒', title: 'Secure Checkout',   desc: 'Multiple payment options with end-to-end encrypted transactions.' },
    { icon: '🌱', title: 'Support Local',     desc: 'Every purchase directly supports a small business in your community.' },
    { icon: '✅', title: 'Verified Sellers',  desc: 'All sellers are screened and admin-approved before going live.' }
  ];

  steps = [
    { step: '01', icon: '🔍', title: 'Browse Products',    desc: 'Explore thousands of local listings — no account needed.' },
    { step: '02', icon: '🛒', title: 'Add to Cart',        desc: 'Pick your favourites and build your order with ease.' },
    { step: '03', icon: '🎉', title: 'Checkout & Enjoy',   desc: 'Sign in, pay securely, and get fast local delivery.' }
  ];

  sellerPerks = [
    { icon: '📸', text: 'AI generates your product listing from a photo' },
    { icon: '📦', text: 'Manage products, orders & earnings in one place' },
    { icon: '👥', text: 'Reach thousands of local buyers instantly' },
    { icon: '🆓', text: 'Free to join — no listing fees' }
  ];

  private observer: IntersectionObserver | null = null;
  private counterTimers: ReturnType<typeof setInterval>[] = [];

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.isLoggedIn = this.auth.isAuthenticated();
    this.initScrollReveal();
    this.animateCounters();
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    this.counterTimers.forEach(t => clearInterval(t));
  }

  goToProducts(): void  { this.router.navigate(['/products']); }
  goToRegister(): void  { this.router.navigate(['/register']); }
  goToDashboard(): void {
    const role = this.auth.getRole();
    if (role === 'SELLER') this.router.navigate(['/seller/dashboard']);
    else if (role === 'ADMIN') this.router.navigate(['/admin/dashboard']);
    else this.router.navigate(['/products']);
  }

  private initScrollReveal(): void {
    this.observer = new IntersectionObserver(
      entries => entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); }),
      { threshold: 0.12 }
    );
    // Wait for Angular to render template
    setTimeout(() => {
      document.querySelectorAll('.reveal').forEach(el => this.observer?.observe(el));
    }, 120);
  }

  private animateCounters(): void {
    const duration = 2200;
    const fps      = 60;
    const frames   = (duration / 1000) * fps;

    this.counters.forEach(counter => {
      const increment = counter.target / frames;
      const timer = setInterval(() => {
        counter.value = Math.min(Math.round(counter.value + increment), counter.target);
        if (counter.value >= counter.target) clearInterval(timer);
      }, 1000 / fps);
      this.counterTimers.push(timer);
    });
  }

  formatCounter(value: number): string {
    if (value >= 1000) return (value / 1000).toFixed(value % 1000 === 0 ? 0 : 1) + 'k';
    return value.toString();
  }
}
