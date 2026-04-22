import { Injectable } from '@angular/core';
import {
  CanActivate, Router,
  ActivatedRouteSnapshot, RouterStateSnapshot
} from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {

    const token   = localStorage.getItem('zuply_token');
    const userStr = localStorage.getItem('zuply_user');

    if (!token || !userStr) {
      this.router.navigate(['/login']);
      return false;
    }

    const user         = JSON.parse(userStr);
    const requiredRole = route.data['role'];

    if (requiredRole && user.role !== requiredRole) {
      this.router.navigate(['/']);
      return false;
    }

    return true;
  }
}
