import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  email        = '';
  password     = '';
  loading      = false;
  errorMsg     = '';
  showPassword = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

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
          const role      = res.data.role;
          const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

          // If there's a returnUrl (e.g. came from checkout), go there first
          if (returnUrl) {
            this.router.navigateByUrl(returnUrl);
          } else if (role === 'SELLER') {
            this.router.navigate(['/seller/dashboard']);
          } else if (role === 'ADMIN') {
            this.router.navigate(['/admin/dashboard']);
          } else {
            this.router.navigate(['/products']);
          }
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
