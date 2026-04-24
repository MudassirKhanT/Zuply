import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, SellerDashboard, SellerOrder, Product } from '../models';

@Injectable({ providedIn: 'root' })
export class SellerService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<ApiResponse<SellerDashboard>> {
    return this.http.get<ApiResponse<SellerDashboard>>(`${this.API}/seller/dashboard`);
  }

  getMyProducts(): Observable<ApiResponse<Product[]>> {
    return this.http.get<ApiResponse<Product[]>>(`${this.API}/seller/products`);
  }

  getMyOrders(): Observable<ApiResponse<SellerOrder[]>> {
    return this.http.get<ApiResponse<SellerOrder[]>>(`${this.API}/seller/orders`);
  }

  updateOrderStatus(orderId: number, status: string): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(
      `${this.API}/seller/orders/${orderId}/status?status=${status}`, {}
    );
  }

  registerSeller(payload: { storeName: string; location: string; pincode: string }): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/seller/register`, payload);
  }
}
