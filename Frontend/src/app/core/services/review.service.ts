import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, ReviewRequest, ReviewsResponse, Review } from '../models';

@Injectable({ providedIn: 'root' })
export class ReviewService {

  constructor(private http: HttpClient) {}

  getReviews(productId: number): Observable<ApiResponse<ReviewsResponse>> {
    return this.http.get<ApiResponse<ReviewsResponse>>(
      `${environment.apiUrl}/products/${productId}/reviews`
    );
  }

  addReview(productId: number, req: ReviewRequest): Observable<ApiResponse<Review>> {
    return this.http.post<ApiResponse<Review>>(
      `${environment.apiUrl}/products/${productId}/reviews`, req
    );
  }
}
