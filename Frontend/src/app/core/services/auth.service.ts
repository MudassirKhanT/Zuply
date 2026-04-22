import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, LoginRequest, RegisterRequest, LoginResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private url = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  register(req: RegisterRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.url}/register`, req);
  }

  login(req: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.url}/login`, req).pipe(
      tap(res => {
        if (res.success && res.data) {
          localStorage.setItem('zuply_token', res.data.token);
          localStorage.setItem('zuply_user', JSON.stringify({
            name:  res.data.name,
            email: res.data.email,
            role:  res.data.role
          }));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('zuply_token');
    localStorage.removeItem('zuply_user');
  }

  isLoggedIn(): boolean    { return !!localStorage.getItem('zuply_token'); }
  getCurrentUser(): any    { const u = localStorage.getItem('zuply_user'); return u ? JSON.parse(u) : null; }
  getRole(): string | null { return this.getCurrentUser()?.role ?? null; }
  isCustomer(): boolean    { return this.getRole() === 'CUSTOMER'; }
  isSeller(): boolean      { return this.getRole() === 'SELLER'; }
  isAdmin(): boolean       { return this.getRole() === 'ADMIN'; }
  getUserName(): string    { return this.getCurrentUser()?.name ?? ''; }
  getUserInitial(): string { return this.getUserName().charAt(0).toUpperCase() || 'U'; }
}
