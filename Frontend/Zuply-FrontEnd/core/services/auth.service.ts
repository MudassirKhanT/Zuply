import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, LoginRequest, LoginResponse, RegisterRequest, UserProfile, CurrentUser } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly API = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(this.loadUser());

  constructor(private http: HttpClient) {}

  private loadUser(): CurrentUser | null {
    const raw = localStorage.getItem('zuply_user');
    return raw ? JSON.parse(raw) : null;
  }

  login(payload: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.API}/auth/login`, payload).pipe(
      tap(res => {
        if (res.success) {
          localStorage.setItem('zuply_token', res.data.token);
          const user: CurrentUser = { token: res.data.token, role: res.data.role, name: res.data.name, email: res.data.email };
          localStorage.setItem('zuply_user', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
      })
    );
  }

  register(payload: RegisterRequest): Observable<ApiResponse<UserProfile>> {
    return this.http.post<ApiResponse<UserProfile>>(`${this.API}/auth/register`, payload);
  }

  logout(): void {
    localStorage.removeItem('zuply_token');
    localStorage.removeItem('zuply_user');
    this.currentUserSubject.next(null);
  }

  getCurrentUser(): CurrentUser | null {
    return this.currentUserSubject.value;
  }

  getUserName(): string {
    return this.currentUserSubject.value?.name || '';
  }

  getRole(): string {
    return this.currentUserSubject.value?.role || '';
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('zuply_token');
  }

  getToken(): string | null {
    return localStorage.getItem('zuply_token');
  }
}
