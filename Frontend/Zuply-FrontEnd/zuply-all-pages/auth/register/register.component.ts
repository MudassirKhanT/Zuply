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
  loading      = false;
  errorMsg     = '';
  showPassword = false;

  roles: Array<'CUSTOMER' | 'SELLER' | 'ADMIN'> = ['CUSTOMER', 'SELLER', 'ADMIN'];

  constructor(private auth: AuthService, private router: Router) {}

  hasUpper()   { return /[A-Z]/.test(this.password); }
  hasLower()   { return /[a-z]/.test(this.password); }
  hasDigit()   { return /\d/.test(this.password); }
  hasSpecial() { return /[@$!%*?&]/.test(this.password); }

  get passwordStrength(): { pct: number; label: string; color: string } {
    const p = this.password;
    let score = 0;
    if (p.length >= 8)                    score++;
    if (/[A-Z]/.test(p))                  score++;
    if (/[a-z]/.test(p))                  score++;
    if (/\d/.test(p))                     score++;
    if (/[@$!%*?&]/.test(p))              score++;

    if (score <= 1) return { pct: 20,  label: 'Weak',   color: '#e74c3c' };
    if (score === 2) return { pct: 40,  label: 'Fair',   color: '#e67e22' };
    if (score === 3) return { pct: 60,  label: 'Good',   color: '#f1c40f' };
    if (score === 4) return { pct: 80,  label: 'Strong', color: '#2ecc71' };
    return              { pct: 100, label: 'Very Strong', color: '#27ae60' };
  }

  onRegister(): void {
    this.errorMsg = '';

    if (!this.name || !this.email || !this.password || !this.phone) {
      this.errorMsg = 'Please fill all required fields.'; return;
    }
    if (!/^[a-zA-Z\s]{2,50}$/.test(this.name)) {
      this.errorMsg = 'Name must be 2–50 characters with letters and spaces only.'; return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email)) {
      this.errorMsg = 'Please enter a valid email address.'; return;
    }
    if (!/^[6-9][0-9]{9}$/.test(this.phone)) {
      this.errorMsg = 'Phone must be a valid 10-digit Indian mobile number.'; return;
    }
    if (this.password.length < 8) {
      this.errorMsg = 'Password must be at least 8 characters.'; return;
    }
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/.test(this.password)) {
      this.errorMsg = 'Password must include uppercase, lowercase, a digit, and a special character (@$!%*?&).'; return;
    }

    this.loading = true;
    this.auth.register({
      name: this.name, email: this.email,
      password: this.password, phone: this.phone, role: this.role,
      storeName: this.storeName || undefined
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
