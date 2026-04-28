import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, AdminStats, AdminReport } from '../models';

@Injectable({ providedIn: 'root' })
export class AdminService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<ApiResponse<AdminStats>> {
    return this.http.get<ApiResponse<AdminStats>>(`${this.API}/admin/dashboard`);
  }

  // ── Seller Management ────────────────────────────────────────────────────

  getSellers(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.API}/admin/sellers`);
  }

  approveSeller(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.API}/admin/sellers/${id}/approve`, {});
  }

  suspendSeller(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.API}/admin/sellers/${id}/suspend`, {});
  }

  deleteSeller(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.API}/admin/sellers/${id}`);
  }

  // ── Product Management ───────────────────────────────────────────────────

  getProducts(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.API}/admin/products`);
  }

  approveProduct(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.API}/admin/products/${id}/approve`, {});
  }

  rejectProduct(id: number): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(`${this.API}/admin/products/${id}/reject`, {});
  }

  updateProduct(id: number, data: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.API}/admin/products/${id}`, data);
  }

  deleteProduct(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.API}/admin/products/${id}`);
  }

  // ── Reports ──────────────────────────────────────────────────────────────

  getReports(): Observable<ApiResponse<AdminReport>> {
    return this.http.get<ApiResponse<AdminReport>>(`${this.API}/admin/reports`);
  }
}
