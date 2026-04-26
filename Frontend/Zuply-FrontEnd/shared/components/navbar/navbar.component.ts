import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  cartCount      = 0;
  mobileMenuOpen = false;
  userDropOpen   = false;

  constructor(
    public auth: AuthService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartService.cartCount$.subscribe(c => this.cartCount = c);
    if (this.auth.isAuthenticated()) {
      this.cartService.getCart().subscribe();
    }
  }

  logout(): void {
    this.auth.logout();
    this.mobileMenuOpen = false;
    this.userDropOpen   = false;
    this.router.navigate(['/login']);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: Event): void {
    const target = e.target as HTMLElement;
    if (!target.closest('.user-menu')) {
      this.userDropOpen = false;
    }
  }

  get role():     string  { return this.auth.getRole(); }
  get isLoggedIn():boolean { return this.auth.isAuthenticated(); }
  get userName(): string  { return this.auth.getUserName(); }
  get userInitial(): string { return (this.userName?.charAt(0) || 'U').toUpperCase(); }
}
