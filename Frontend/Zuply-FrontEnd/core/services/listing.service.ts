import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, UploadResponse, ListingResponse, ListingEditRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class ListingService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  uploadImage(file: File): Observable<ApiResponse<UploadResponse>> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<UploadResponse>>(`${this.API}/upload`, form);
  }

  generateListing(imageId: number): Observable<ApiResponse<ListingResponse>> {
    return this.http.post<ApiResponse<ListingResponse>>(`${this.API}/listing/generate/${imageId}`, {});
  }

  getListing(imageId: number): Observable<ApiResponse<ListingResponse>> {
    return this.http.get<ApiResponse<ListingResponse>>(`${this.API}/listing/${imageId}`);
  }

  editListing(productId: number, payload: ListingEditRequest): Observable<ApiResponse<ListingResponse>> {
    return this.http.put<ApiResponse<ListingResponse>>(`${this.API}/listing/${productId}`, payload);
  }

  publishListing(productId: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/listing/${productId}/publish`, {});
  }
}
