import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';
import { WishlistService } from '../../../core/services/wishlist.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  cartCount     = 0;
  wishlistCount = 0;
  mobileMenuOpen = false;
  accountMenuOpen = false;

  constructor(
    public auth: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartService.cartCount$.subscribe(c => this.cartCount = c);
    this.wishlistService.wishlistCount$.subscribe(c => this.wishlistCount = c);
    // Only CUSTOMER role has access to /api/cart and /api/wishlist — sellers/admins get 403 otherwise
    if (this.auth.isAuthenticated() && this.auth.getRole() === 'CUSTOMER') {
      this.cartService.getCart().subscribe();
      this.wishlistService.getWishlist().subscribe();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    const target = e.target as HTMLElement;
    if (!target.closest('.account-wrap')) {
      this.accountMenuOpen = false;
    }
  }

  logout(): void {
    this.auth.logout();
    this.accountMenuOpen = false;
    this.mobileMenuOpen  = false;
    this.router.navigate(['/login']);
  }

  get role(): string     { return this.auth.getRole(); }
  get isLoggedIn(): boolean { return this.auth.isAuthenticated(); }
  get userName(): string { return this.auth.getUserName(); }

  get userInitial(): string {
    return this.userName?.charAt(0)?.toUpperCase() || 'U';
  }
}
