import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AdminStats } from '../models';

@Injectable({ providedIn: 'root' })
export class AdminService {

  private url = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<ApiResponse<AdminStats>> {
    return this.http.get<ApiResponse<AdminStats>>(`${this.url}/dashboard`);
  }

  // ── Seller Management ────────────────────────────────────────────────────

  getSellers(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.url}/sellers`);
  }

  approveSeller(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.url}/sellers/${id}/approve`, {});
  }

  suspendSeller(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.url}/sellers/${id}/suspend`, {});
  }

  deleteSeller(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.url}/sellers/${id}`);
  }

  // ── Product Management ───────────────────────────────────────────────────

  getProducts(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.url}/products`);
  }

  updateProduct(id: number, data: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.url}/products/${id}`, data);
  }

  deleteProduct(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.url}/products/${id}`);
  }

  // ── Reports ──────────────────────────────────────────────────────────────

  getReports(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.url}/reports`);
  }
}
