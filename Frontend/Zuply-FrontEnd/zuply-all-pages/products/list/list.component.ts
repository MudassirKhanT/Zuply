// src/app/pages/products/list/list.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { Product, Category } from '../../../core/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {

  products: Product[]    = [];
  categories: Category[] = [];
  selectedCategory = '';
  searchQuery      = '';
  sortBy           = '';
  loading          = true;
  cartMsg          = '';
  cartMsgType: 'success' | 'error' = 'success';
  private cartMsgTimer: any;

  sortOptions = [
    { label: 'Default',        value: '' },
    { label: 'Price: Low to High', value: 'price_asc' },
    { label: 'Price: High to Low', value: 'price_desc' },
    { label: 'Nearest First',  value: 'distance' },
  ];

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getCategories().subscribe({
      next: res => { if (res.success) this.categories = res.data; }
    });
    this.route.queryParams.subscribe(params => {
      this.searchQuery      = params['name']     || '';
      this.selectedCategory = params['category'] || '';
      this.loadProducts();
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAll(undefined, this.searchQuery || undefined, this.sortBy || undefined).subscribe({
      next: res => {
        if (res.success) {
          this.products = this.selectedCategory
            ? res.data.filter(p => p.category === this.selectedCategory)
            : res.data;
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  onCategorySelect(name: string): void {
    this.selectedCategory = this.selectedCategory === name ? '' : name;
    this.loadProducts();
  }

  onSortChange(): void { this.loadProducts(); }

  addToCart(product: Product): void {
    this.cartService.addToCart(product.id).subscribe({
      next: res => {
        if (res.success) this.showCartMsg(`"${product.name}" added to cart!`, 'success');
        else this.showCartMsg(res.message || 'Could not add to cart.', 'error');
      },
      error: (err: HttpErrorResponse) => {
        const msg = err.error?.message || 'Could not add to cart. Please try again.';
        this.showCartMsg(msg, 'error');
      }
    });
  }

  goToDetail(product: Product): void {
    this.router.navigate(['/products', product.id]);
  }

  private showCartMsg(msg: string, type: 'success' | 'error'): void {
    this.cartMsg = msg;
    this.cartMsgType = type;
    clearTimeout(this.cartMsgTimer);
    this.cartMsgTimer = setTimeout(() => this.cartMsg = '', 3000);
  }
}
