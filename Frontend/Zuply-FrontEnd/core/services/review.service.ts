import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models';

export interface Review {
  id: number;
  customerName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface ReviewsResponse {
  reviews: Review[];
  averageRating: number;
  count: number;
}

export interface ReviewRequest {
  rating: number;
  comment: string;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly API = environment.apiUrl;
  constructor(private http: HttpClient) {}

  getReviews(productId: number): Observable<ApiResponse<ReviewsResponse>> {
    return this.http.get<ApiResponse<ReviewsResponse>>(`${this.API}/products/${productId}/reviews`);
  }

  addReview(productId: number, req: ReviewRequest): Observable<ApiResponse<Review>> {
    return this.http.post<ApiResponse<Review>>(`${this.API}/products/${productId}/reviews`, req);
  }
}
