import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, UserProfile } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<ApiResponse<UserProfile>> {
    return this.http.get<ApiResponse<UserProfile>>(`${this.API}/users/profile`);
  }

  updateProfile(payload: UserProfile): Observable<ApiResponse<UserProfile>> {
    return this.http.put<ApiResponse<UserProfile>>(`${this.API}/users/profile`, payload);
  }
}
