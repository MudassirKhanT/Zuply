// src/app/pages/auth/login/login.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  email       = '';
  password    = '';
  role        = 'CUSTOMER';
  loading     = false;
  errorMsg    = '';
  showPassword = false;

  roles = ['CUSTOMER', 'SELLER', 'ADMIN'];

  constructor(private auth: AuthService, private router: Router) {}

  onLogin(): void {
    if (!this.email || !this.password) {
      this.errorMsg = 'Please enter email and password.';
      return;
    }
    this.loading  = true;
    this.errorMsg = '';

    this.auth.login({ email: this.email, password: this.password }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          const role = res.data.role;
          if (role === 'SELLER') this.router.navigate(['/seller/dashboard']);
          else if (role === 'ADMIN') this.router.navigate(['/admin/dashboard']);
          else this.router.navigate(['/']);
        } else {
          this.errorMsg = res.message || 'Login failed.';
        }
      },
      error: err => {
        this.loading  = false;
        this.errorMsg = err.error?.message || 'Invalid email or password.';
      }
    });
  }
}
