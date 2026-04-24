import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const requiredRole: string = route.data['role'];
    const userRaw = localStorage.getItem('zuply_user');
    if (!userRaw) { this.router.navigate(['/login']); return false; }

    const user = JSON.parse(userRaw);
    if (user.role === requiredRole) return true;

    this.router.navigate(['/']);
    return false;
  }
}
