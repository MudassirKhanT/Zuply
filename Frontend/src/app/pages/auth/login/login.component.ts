import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  form         = { email: '', password: '' };
  errorMsg     = '';
  loading      = false;
  showPassword = false;
  returnUrl    = '';

  constructor(
    private auth: AuthService,
    private cartService: CartService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '';
    if (this.auth.isLoggedIn()) this.router.navigate([this.returnUrl || '/']);
  }

  onSubmit(): void {
    if (!this.form.email || !this.form.password) { this.errorMsg = 'Please fill in all fields.'; return; }
    this.loading  = true;
    this.errorMsg = '';
    this.auth.login({ email: this.form.email, password: this.form.password }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          if (res.data.role === 'CUSTOMER') this.cartService.getCart().subscribe();
          this.router.navigate([this.returnUrl || (res.data.role === 'SELLER' ? '/seller/dashboard' : res.data.role === 'ADMIN' ? '/admin/dashboard' : '/')]);
        } else {
          this.errorMsg = res.message || 'Login failed.';
        }
      },
      error: err => {
        this.loading  = false;
        this.errorMsg = err?.error?.message || 'Invalid email or password.';
      }
    });
  }
}
