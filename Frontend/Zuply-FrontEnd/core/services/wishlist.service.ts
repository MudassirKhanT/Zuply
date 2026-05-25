import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, WishlistItem } from '../models';

@Injectable({ providedIn: 'root' })
export class WishlistService {

  private readonly API = environment.apiUrl;

  private countSubject = new BehaviorSubject<number>(0);
  wishlistCount$ = this.countSubject.asObservable();

  constructor(private http: HttpClient) {}

  getWishlist(): Observable<ApiResponse<WishlistItem[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.API}/wishlist`).pipe(
      map(res => {
        if (res.success) {
          const mapped: WishlistItem[] = res.data.map((w: any) => ({
            wishlistId: w.id,
            productId:   w.product?.id   || w.productId,
            productName: w.product?.name || w.productName || '',
            price:       w.product?.price || w.price || 0,
            sellerName:  w.product?.sellerName || w.sellerName || '',
            imageUrl:    w.product?.imageUrl || w.imageUrl || '',
          }));
          return { ...res, data: mapped };
        }
        return res as any;
      }),
      tap(res => { if (res.success) this.countSubject.next(res.data.length); })
    );
  }

  addToWishlist(productId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/wishlist/${productId}`, {}).pipe(
      tap(() => this.countSubject.next(this.countSubject.value + 1))
    );
  }

  removeFromWishlist(productId: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.API}/wishlist/${productId}`).pipe(
      tap(() => this.countSubject.next(Math.max(0, this.countSubject.value - 1)))
    );
  }
}
