import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class SellerService {

  private url = `${environment.apiUrl}/seller`;

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.url}/dashboard`);
  }

  getMyProducts(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.url}/products`);
  }

  getMyOrders(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.url}/orders`);
  }

  updateOrderStatus(id: number, status: string): Observable<ApiResponse<any>> {
    return this.http.patch<ApiResponse<any>>(
      `${this.url}/orders/${id}/status`, { status }
    );
  }
}
