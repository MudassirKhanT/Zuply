import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, Order, CheckoutPayload } from '../models';

@Injectable({ providedIn: 'root' })
export class OrderService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  checkout(payload: CheckoutPayload): Observable<ApiResponse<Order>> {
    return this.http.post<ApiResponse<Order>>(`${this.API}/orders`, payload);
  }

  getOrders(): Observable<ApiResponse<Order[]>> {
    return this.http.get<ApiResponse<Order[]>>(`${this.API}/orders`);
  }

  getOrderById(id: number): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.API}/orders/${id}`);
  }
}
