import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  form         = { name: '', email: '', phone: '', password: '', role: 'CUSTOMER' as 'CUSTOMER' | 'SELLER' | 'ADMIN' };
  errorMsg     = '';
  successMsg   = '';
  loading      = false;
  showPassword = false;

  constructor(private auth: AuthService, private router: Router) {
    if (this.auth.isLoggedIn()) this.router.navigate(['/']);
  }

  onSubmit(): void {
    if (!this.form.name || !this.form.email || !this.form.password || !this.form.phone) {
      this.errorMsg = 'Please fill in all fields.'; return;
    }
    this.loading  = true;
    this.errorMsg = '';
    this.auth.register(this.form).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          this.successMsg = 'Account created! Redirecting to login…';
          setTimeout(() => this.router.navigate(['/login']), 1500);
        } else {
          this.errorMsg = res.message || 'Registration failed.';
        }
      },
      error: err => {
        this.loading  = false;
        this.errorMsg = err?.error?.message || 'Registration failed. Try a different email.';
      }
    });
  }
}
