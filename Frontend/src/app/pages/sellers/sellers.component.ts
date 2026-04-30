import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService } from '../../core/services/product.service';
import { Product, SellerSummary } from '../../core/models';

@Component({
  selector: 'app-sellers',
  templateUrl: './sellers.component.html',
  styleUrls: ['./sellers.component.scss']
})
export class SellersComponent implements OnInit {

  sellers: SellerSummary[] = [];
  loading = true;
  searchQuery = '';

  get filteredSellers(): SellerSummary[] {
    const q = this.searchQuery.toLowerCase().trim();
    if (!q) return this.sellers;
    return this.sellers.filter(s =>
      s.sellerName.toLowerCase().includes(q) ||
      s.pincode.includes(q) ||
      s.categories.some(c => c.toLowerCase().includes(q))
    );
  }

  constructor(private productService: ProductService, private router: Router) {}

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: res => {
        if (res.success) this.buildSellerList(res.data);
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  private buildSellerList(products: Product[]): void {
    const map = new Map<number, SellerSummary>();
    products.forEach(p => {
      if (!p.sellerId || !p.sellerName) return;
      if (!map.has(p.sellerId)) {
        map.set(p.sellerId, {
          sellerId: p.sellerId,
          sellerName: p.sellerName,
          pincode: p.sellerPincode || p.pincode || '',
          productCount: 0,
          categories: [],
          sampleImage: p.imageUrl
        });
      }
      const s = map.get(p.sellerId)!;
      s.productCount++;
      const cat = p.categoryName || p.category || '';
      if (cat && !s.categories.includes(cat)) s.categories.push(cat);
      if (!s.sampleImage && p.imageUrl) s.sampleImage = p.imageUrl;
    });
    this.sellers = Array.from(map.values()).sort((a, b) => b.productCount - a.productCount);
  }

  goToSeller(id: number): void {
    this.router.navigate(['/sellers', id]);
  }
}
