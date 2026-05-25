// src/app/pages/auth/register/register.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  name      = '';
  email     = '';
  password  = '';
  phone     = '';
  storeName = '';
  role: 'CUSTOMER' | 'SELLER' = 'CUSTOMER';
  loading   = false;
  errorMsg  = '';
  showPassword = false;

  // touched flags — show inline errors only after user visits the field
  nameTouched     = false;
  emailTouched    = false;
  phoneTouched    = false;
  passwordTouched = false;

  roles: Array<'CUSTOMER' | 'SELLER'> = ['CUSTOMER', 'SELLER'];

  // ── Validators ────────────────────────────────────────────────
  get isEmailValid(): boolean {
    return /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/.test(this.email.trim());
  }

  /** Indian mobile: starts with 6-9, exactly 10 digits */
  get isPhoneValid(): boolean {
    return /^[6-9]\d{9}$/.test(this.phone.trim());
  }

  /** Min 8 chars, uppercase, lowercase, digit, and special char — must match backend @Pattern */
  get isPasswordValid(): boolean {
    return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(this.password);
  }

  get passwordStrength(): 'weak' | 'medium' | 'strong' {
    if (!this.password) return 'weak';
    let score = 0;
    if (this.password.length >= 8)          score++;
    if (this.password.length >= 12)         score++;
    if (/[A-Z]/.test(this.password))        score++;
    if (/[a-z]/.test(this.password))        score++;
    if (/[0-9]/.test(this.password))        score++;
    if (/[^A-Za-z0-9]/.test(this.password)) score++;
    if (score <= 2) return 'weak';
    if (score <= 4) return 'medium';
    return 'strong';
  }

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const roleParam = this.route.snapshot.queryParamMap.get('role');
    if (roleParam === 'SELLER') this.role = 'SELLER';
  }

  onRegister(): void {
    // Mark every field touched so all errors become visible
    this.nameTouched = this.emailTouched = this.phoneTouched = this.passwordTouched = true;

    if (!this.name.trim()) {
      this.errorMsg = 'Please enter your full name.'; return;
    }
    if (!this.isEmailValid) {
      this.errorMsg = 'Please enter a valid email address (e.g. user@example.com).'; return;
    }
    if (!this.isPhoneValid) {
      this.errorMsg = 'Phone must be a 10-digit Indian mobile number starting with 6–9.'; return;
    }
    if (!this.isPasswordValid) {
      this.errorMsg = 'Password must be at least 8 characters and include uppercase, lowercase, a digit, and a special character (@$!%*?&).'; return;
    }

    this.loading = true; this.errorMsg = '';

    this.auth.register({
      name: this.name.trim(), email: this.email.trim(),
      password: this.password, phone: this.phone.trim(), role: this.role
    }).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          this.router.navigate(['/login'], returnUrl ? { queryParams: { returnUrl } } : {});
        } else {
          this.errorMsg = res.message || 'Registration failed.';
        }
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message || 'Registration failed. Try again.';
      }
    });
  }
}
