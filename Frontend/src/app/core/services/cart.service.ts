import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, CartResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class CartService {

  private url          = `${environment.apiUrl}/cart`;
  private cartCount$   = new BehaviorSubject<number>(0);
  cartCount            = this.cartCount$.asObservable();

  constructor(private http: HttpClient) {}

  getCart(): Observable<ApiResponse<CartResponse>> {
    return this.http.get<ApiResponse<CartResponse>>(this.url).pipe(
      tap(res => {
        if (res.success) this.cartCount$.next(res.data?.items?.length ?? 0);
      })
    );
  }

  addToCart(productId: number, quantity = 1): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(this.url, { productId, quantity }).pipe(
      tap(() => this.cartCount$.next(this.cartCount$.value + 1))
    );
  }

  updateQuantity(itemId: number, quantity: number): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.url}/${itemId}`, { quantity });
  }

  removeItem(itemId: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.url}/${itemId}`).pipe(
      tap(() => this.cartCount$.next(Math.max(0, this.cartCount$.value - 1)))
    );
  }

  resetCount(): void { this.cartCount$.next(0); }
}
