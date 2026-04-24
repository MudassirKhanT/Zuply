import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, CartResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class CartService {

  private readonly API = environment.apiUrl;
  private countSubject = new BehaviorSubject<number>(0);
  cartCount$ = this.countSubject.asObservable();

  constructor(private http: HttpClient) {}

  getCart(): Observable<ApiResponse<CartResponse>> {
    return this.http.get<ApiResponse<CartResponse>>(`${this.API}/cart`).pipe(
      tap(res => { if (res.success) this.countSubject.next(res.data.items?.length || 0); })
    );
  }

  addToCart(productId: number, quantity = 1): Observable<ApiResponse<CartResponse>> {
    return this.http.post<ApiResponse<CartResponse>>(`${this.API}/cart`, { productId, quantity }).pipe(
      tap(res => { if (res.success) this.countSubject.next(res.data.items?.length || 0); })
    );
  }

  updateQuantity(itemId: number, quantity: number): Observable<ApiResponse<CartResponse>> {
    return this.http.put<ApiResponse<CartResponse>>(`${this.API}/cart/${itemId}?quantity=${quantity}`, {}).pipe(
      tap(res => { if (res.success) this.countSubject.next(res.data.items?.length || 0); })
    );
  }

  removeItem(itemId: number): Observable<ApiResponse<CartResponse>> {
    return this.http.delete<ApiResponse<CartResponse>>(`${this.API}/cart/${itemId}`).pipe(
      tap(res => { if (res.success) this.countSubject.next(res.data.items?.length || 0); })
    );
  }

  resetCount(): void {
    this.countSubject.next(0);
  }
}
