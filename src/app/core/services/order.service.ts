import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Order, CheckoutRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class OrderService {

  private url = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  checkout(req: CheckoutRequest): Observable<ApiResponse<Order>> {
    return this.http.post<ApiResponse<Order>>(this.url, req);
  }

  getOrders(): Observable<ApiResponse<Order[]>> {
    return this.http.get<ApiResponse<Order[]>>(this.url);
  }

  getOrderById(id: number): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.url}/${id}`);
  }
}
