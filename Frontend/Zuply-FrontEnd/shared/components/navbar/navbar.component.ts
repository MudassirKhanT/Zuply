import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  cartCount = 0;
  mobileMenuOpen = false;

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
    this.router.navigate(['/login']);
  }

  get role(): string { return this.auth.getRole(); }
  get isLoggedIn(): boolean { return this.auth.isAuthenticated(); }
  get userName(): string { return this.auth.getUserName(); }
}
