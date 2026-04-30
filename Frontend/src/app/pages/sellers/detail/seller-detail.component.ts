import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../../core/services/product.service';
import { CartService } from '../../../core/services/cart.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../core/models';

@Component({
  selector: 'app-seller-detail',
  templateUrl: './seller-detail.component.html',
  styleUrls: ['./seller-detail.component.scss']
})
export class SellerDetailComponent implements OnInit {

  sellerId  = 0;
  sellerName = '';
  pincode    = '';
  products: Product[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private cartService: CartService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.sellerId = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getAll().subscribe({
      next: res => {
        if (res.success) {
          this.products = res.data.filter((p: Product) => p.sellerId === this.sellerId);
          if (this.products.length > 0) {
            this.sellerName = this.products[0].sellerName || 'Seller';
            this.pincode    = this.products[0].sellerPincode || this.products[0].pincode || '';
          }
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  goToProduct(id: number): void { this.router.navigate(['/products', id]); }

  addToCart(product: Product): void {
    if (!this.auth.isLoggedIn()) { this.router.navigate(['/login']); return; }
    this.cartService.addToCart(product.id).subscribe();
  }
}
