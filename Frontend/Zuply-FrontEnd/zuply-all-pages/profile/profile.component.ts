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
  profilePic: string | null = null;

  private readonly PFP_KEY = 'zuply_pfp';

  constructor(
    private auth: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.profilePic = localStorage.getItem(this.PFP_KEY);
    const authUser = this.auth.getCurrentUser();
    this.userService.getProfile().subscribe({
      next: res => {
        if (res.success) this.user = { ...res.data, role: authUser?.role };
        else              this.user = authUser;
      },
      error: () => { this.user = authUser; }
    });
  }

  onPfpChange(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e) => {
      this.profilePic = e.target?.result as string;
      localStorage.setItem(this.PFP_KEY, this.profilePic);
    };
    reader.readAsDataURL(file);
  }

  removePfp(): void {
    this.profilePic = null;
    localStorage.removeItem(this.PFP_KEY);
  }

  get userInitial(): string {
    return this.user?.name?.charAt(0)?.toUpperCase() || 'U';
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
