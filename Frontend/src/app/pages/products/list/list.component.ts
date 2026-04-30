import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product, Category } from '../../../core/models';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  allProducts: Product[] = [];
  filtered:    Product[] = [];
  categories:  Category[] = [];

  selectedCategory = '';
  searchQuery      = '';
  sortBy           = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  currentPincode   = '';
  pincodeInput     = '';
  loading          = true;
  showLocationModal = false;
  wishlistedIds    = new Set<number>();
  toastMsg         = '';
  toastTimer: any;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.productService.getCategories().subscribe({
      next: res => { if (res.success) this.categories = res.data; }
    });
    if (this.auth.isLoggedIn() && this.auth.isCustomer()) {
      this.wishlistService.getWishlist().subscribe({
        next: res => {
          if (res.success) res.data.forEach((item: any) => this.wishlistedIds.add(item.productId ?? item.product?.id));
        }
      });
    }
    this.route.queryParams.subscribe(params => {
      if (params['name'])     this.searchQuery      = params['name'];
      if (params['category']) this.selectedCategory = params['category'];
      if (params['pincode'])  { this.currentPincode = params['pincode']; this.pincodeInput = params['pincode']; }
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAll(this.currentPincode || undefined, undefined, this.sortBy || undefined).subscribe({
      next: res => {
        this.allProducts = res.success ? res.data : [];
        this.applyFilters();
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  applyFilters(): void {
    let result = [...this.allProducts];

    if (this.selectedCategory) {
      result = result.filter(p => (p.categoryName || p.category) === this.selectedCategory);
    }
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(p => p.name.toLowerCase().includes(q) || (p.description || '').toLowerCase().includes(q));
    }
    if (this.minPrice !== null && this.minPrice > 0) {
      result = result.filter(p => p.price >= this.minPrice!);
    }
    if (this.maxPrice !== null && this.maxPrice > 0) {
      result = result.filter(p => p.price <= this.maxPrice!);
    }
    if (this.sortBy === 'price_asc')  result.sort((a, b) => a.price - b.price);
    if (this.sortBy === 'price_desc') result.sort((a, b) => b.price - a.price);

    this.filtered = result;
  }

  selectCategory(name: string): void {
    this.selectedCategory = name;
    this.applyFilters();
  }

  applyPincode(): void {
    this.currentPincode = this.pincodeInput.trim();
    this.loadProducts();
  }

  clearPincode(): void {
    this.currentPincode = '';
    this.pincodeInput   = '';
    this.loadProducts();
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.searchQuery      = '';
    this.sortBy           = '';
    this.minPrice         = null;
    this.maxPrice         = null;
    this.applyFilters();
  }

  addToCart(product: Product): void {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/products' } }); return;
    }
    this.cartService.addToCart(product.id).subscribe({
      next: res => {
        if (res.success) this.showToast('✓ Added to cart!');
        else this.showToast(res.message || 'Could not add to cart');
      },
      error: () => this.showToast('Failed to add to cart')
    });
  }

  toggleWishlist(product: Product): void {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/products' } }); return;
    }
    const isWishlisted = this.wishlistedIds.has(product.id);
    if (isWishlisted) {
      this.wishlistService.removeFromWishlist(product.id).subscribe({
        next: res => {
          if (res.success) { this.wishlistedIds.delete(product.id); this.showToast('Removed from wishlist'); }
        },
        error: () => this.showToast('Failed to update wishlist')
      });
    } else {
      this.wishlistService.addToWishlist(product.id).subscribe({
        next: res => {
          if (res.success) { this.wishlistedIds.add(product.id); this.showToast('❤️ Added to wishlist!'); }
        },
        error: () => this.showToast('Failed to update wishlist')
      });
    }
  }

  showToast(msg: string): void {
    this.toastMsg = msg;
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => this.toastMsg = '', 2500);
  }

  goToDetail(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }
}
