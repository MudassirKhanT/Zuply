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
      },
      error: () => { this.user = authUser; }
    });
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
