import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';
import { WishlistService } from '../../core/services/wishlist.service';
import { Product, Category } from '../../core/models';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  products: Product[]   = [];
  categories: Category[] = [];
  selectedCategory      = '';
  loading               = true;

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
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
            ? res.data.filter(p => p.category === category)
            : res.data.slice(0, 8);
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
    this.cartService.addToCart(product.id).subscribe({
      next: res => { if (res.success) alert('Added to cart!'); }
    });
  }

  goToDetail(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }

  goToProducts(): void {
    this.router.navigate(['/products']);
  }
}
