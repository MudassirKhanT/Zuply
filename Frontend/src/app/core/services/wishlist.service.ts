import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, WishlistItem } from '../models';

@Injectable({ providedIn: 'root' })
export class WishlistService {

  private url = `${environment.apiUrl}/wishlist`;

  constructor(private http: HttpClient) {}

  getWishlist(): Observable<ApiResponse<WishlistItem[]>> {
    return this.http.get<ApiResponse<WishlistItem[]>>(this.url);
  }

  addToWishlist(productId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.url}/${productId}`, {});
  }

  removeFromWishlist(productId: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.url}/${productId}`);
  }
}