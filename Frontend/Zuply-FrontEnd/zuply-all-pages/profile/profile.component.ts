// src/app/pages/profile/profile.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  user: any = null;
  editing = false;
  saving  = false;
  successMsg = '';
  errorMsg   = '';

  form = { name: '', email: '', phone: '', address: '', pincode: '' };
  original = { ...this.form };

  constructor(
    private auth: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const authUser = this.auth.getCurrentUser();
    this.userService.getProfile().subscribe({
      next: res => {
        if (res.success) {
          this.user = { ...res.data, role: authUser?.role };
        } else {
          this.user = authUser;
        }
        this.populateForm();
      },
      error: () => {
        this.user = authUser;
        this.populateForm();
      }
    });
  }

  private populateForm(): void {
    this.form = {
      name:    this.user?.name    || '',
      email:   this.user?.email   || '',
      phone:   this.user?.phone   || '',
      address: this.user?.address || '',
      pincode: this.user?.pincode || ''
    };
    this.original = { ...this.form };
  }

  startEdit(): void {
    this.editing = true;
    this.successMsg = '';
    this.errorMsg   = '';
  }

  cancelEdit(): void {
    this.form = { ...this.original };
    this.editing = false;
    this.errorMsg = '';
  }

  resetForm(): void {
    this.form = { ...this.original };
  }

  saveProfile(): void {
    this.saving = true;
    this.errorMsg = '';
    this.userService.updateProfile(this.form as any).subscribe({
      next: res => {
        this.saving = false;
        if (res.success) {
          this.user = { ...this.user, ...this.form };
          this.original = { ...this.form };
          this.editing = false;
          this.successMsg = 'Profile updated successfully.';
          setTimeout(() => this.successMsg = '', 3000);
        } else {
          this.errorMsg = res.message || 'Update failed.';
        }
      },
      error: err => {
        this.saving = false;
        this.errorMsg = err?.error?.message || 'Update failed. Please try again.';
      }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
