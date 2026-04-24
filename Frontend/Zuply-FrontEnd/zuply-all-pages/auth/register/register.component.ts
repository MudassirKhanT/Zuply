// src/app/pages/auth/register/register.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  name      = '';
  email     = '';
  password  = '';
  phone     = '';
  storeName = '';
  role: 'CUSTOMER' | 'SELLER' | 'ADMIN' = 'CUSTOMER';
  loading   = false;
  errorMsg  = '';
  showPassword = false;

  roles: Array<'CUSTOMER' | 'SELLER' | 'ADMIN'> = ['CUSTOMER', 'SELLER', 'ADMIN'];

  constructor(private auth: AuthService, private router: Router) {}

  onRegister(): void {
    if (!this.name || !this.email || !this.password || !this.phone) {
      this.errorMsg = 'Please fill all required fields.'; return;
    }
    this.loading = true; this.errorMsg = '';

    this.auth.register({
      name: this.name, email: this.email,
      password: this.password, phone: this.phone, role: this.role
    }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) this.router.navigate(['/login']);
        else this.errorMsg = res.message || 'Registration failed.';
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message || 'Registration failed. Try again.';
      }
    });
  }
}
