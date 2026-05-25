import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, Review, ReviewRequest, ReviewsResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class ReviewService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getReviews(productId: number): Observable<ApiResponse<ReviewsResponse>> {
    return this.http.get<ApiResponse<ReviewsResponse>>(`${this.API}/products/${productId}/reviews`);
  }

  submitReview(productId: number, payload: ReviewRequest): Observable<ApiResponse<Review>> {
    return this.http.post<ApiResponse<Review>>(`${this.API}/products/${productId}/reviews`, payload);
  }
}
