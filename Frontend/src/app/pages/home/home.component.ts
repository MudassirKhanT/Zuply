import { Component, OnInit, AfterViewInit, ElementRef, ViewChildren, QueryList } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';
import { Product, Category, SellerSummary } from '../../core/models';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {

  products: Product[]        = [];
  categories: Category[]     = [];
  localSellers: SellerSummary[] = [];
  selectedCategory           = '';
  loading                    = true;

  features = [
    { icon: '📍', title: 'Local First',       desc: 'Products from sellers near you' },
    { icon: '⚡', title: 'Fast Delivery',      desc: 'Same-day & next-day options' },
    { icon: '✦',  title: 'AI-Powered',         desc: 'Smart search & product tagging' },
    { icon: '🔒', title: 'Secure Payments',    desc: 'UPI, Card & COD supported' },
  ];

  howSteps = [
    { icon: '🔍', title: 'Browse',  desc: 'Search products by category, location, or keyword.' },
    { icon: '🛒', title: 'Add to Cart', desc: 'Pick your items and review the cart.' },
    { icon: '💳', title: 'Pay',     desc: 'Checkout with UPI, card, or cash on delivery.' },
    { icon: '🚚', title: 'Receive', desc: 'Get your order delivered to your doorstep.' },
  ];

  sellSteps = [
    { num: '01', text: 'Upload a product photo' },
    { num: '02', text: 'AI generates listing details' },
    { num: '03', text: 'Review, publish & sell' },
  ];

  @ViewChildren('featuresRef,catsRef,howRef,productsRef,sellRef')
  animatedSections!: QueryList<ElementRef>;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  ngAfterViewInit(): void {
    const observer = new IntersectionObserver(
      entries => entries.forEach(e => {
        if (e.isIntersecting) {
          e.target.classList.add('visible');
          observer.unobserve(e.target);
        }
      }),
      { threshold: 0.12 }
    );
    setTimeout(() => {
      this.animatedSections.forEach(ref => observer.observe(ref.nativeElement));
    }, 100);
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe({
      next: res => { if (res.success) this.categories = res.data; }
    });
  }

  loadProducts(category?: string): void {
    this.loading = true;
    this.productService.getAll().subscribe({
      next: res => {
        if (res.success) {
          this.products = category
            ? res.data.filter((p: Product) => p.categoryName === category || p.category === category)
            : res.data.slice(0, 8);
          if (!category) this.buildLocalSellers(res.data);
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  onCategorySelect(name: string): void {
    this.selectedCategory = this.selectedCategory === name ? '' : name;
    this.loadProducts(this.selectedCategory || undefined);
  }

  addToCart(product: Product): void {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/' } });
      return;
    }
    this.cartService.addToCart(product.id).subscribe();
  }

  goToDetail(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }

  goToProducts(): void {
    this.router.navigate(['/products']);
  }

  private buildLocalSellers(products: Product[]): void {
    const map = new Map<number, SellerSummary>();
    products.forEach(p => {
      if (!p.sellerId || !p.sellerName) return;
      if (!map.has(p.sellerId)) {
        map.set(p.sellerId, { sellerId: p.sellerId, sellerName: p.sellerName,
          pincode: p.sellerPincode || p.pincode || '', productCount: 0, categories: [] });
      }
      map.get(p.sellerId)!.productCount++;
    });
    this.localSellers = Array.from(map.values()).slice(0, 6);
  }
}
