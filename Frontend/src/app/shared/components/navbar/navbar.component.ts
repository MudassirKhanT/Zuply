import { Component, OnInit, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

  searchQuery  = '';
  location     = 'Chennai, 600001';
  showUserMenu = false;
  cartCount$!: Observable<number>;
  pfp          = '';   // base64 profile picture (stored in localStorage)

  get isLoggedIn():  boolean { return this.auth.isLoggedIn(); }
  get isCustomer():  boolean { return this.auth.isCustomer(); }
  get isSeller():    boolean { return this.auth.isSeller(); }
  get isAdmin():     boolean { return this.auth.isAdmin(); }
  get userInitial(): string  { return this.auth.getUserInitial(); }
  get userName():    string  { return this.auth.getUserName(); }

  constructor(
    private auth: AuthService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartCount$ = this.cartService.cartCount;
    if (this.isCustomer) {
      this.cartService.getCart().subscribe();
    }
    this.loadPfp();
  }

  loadPfp(): void {
    const stored = localStorage.getItem('zuply_pfp');
    if (stored) this.pfp = stored;
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/products'], {
        queryParams: { name: this.searchQuery.trim() }
      });
      this.searchQuery = '';
    }
  }

  toggleUserMenu(): void {
    this.loadPfp();    // refresh PFP in case it was just updated
    this.showUserMenu = !this.showUserMenu;
  }
  closeUserMenu():  void { this.showUserMenu = false; }

  logout(): void {
    this.auth.logout();
    this.cartService.resetCount();
    this.pfp = '';
    this.closeUserMenu();
    this.router.navigate(['/']);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.user-menu-wrap')) {
      this.showUserMenu = false;
    }
  }
}
