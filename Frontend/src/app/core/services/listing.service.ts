import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiResponse, UploadResponse,
  ListingResponse, ListingEditRequest, PublishResponse
} from '../models';

@Injectable({ providedIn: 'root' })
export class ListingService {

  private uploadUrl  = `${environment.apiUrl}/upload`;
  private listingUrl = `${environment.apiUrl}/listing`;

  constructor(private http: HttpClient) {}

  uploadImage(file: File): Observable<ApiResponse<UploadResponse>> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ApiResponse<UploadResponse>>(this.uploadUrl, form);
  }

  generateListing(imageId: number): Observable<ApiResponse<ListingResponse>> {
    return this.http.post<ApiResponse<ListingResponse>>(
      `${this.listingUrl}/generate/${imageId}`, {}
    );
  }

  getListing(imageId: number): Observable<ApiResponse<ListingResponse>> {
    return this.http.get<ApiResponse<ListingResponse>>(
      `${this.listingUrl}/${imageId}`
    );
  }

  editListing(productId: number, data: ListingEditRequest): Observable<ApiResponse<ListingResponse>> {
    return this.http.put<ApiResponse<ListingResponse>>(
      `${this.listingUrl}/${productId}`, data
    );
  }

  publishListing(productId: number): Observable<ApiResponse<PublishResponse>> {
    return this.http.post<ApiResponse<PublishResponse>>(
      `${this.listingUrl}/${productId}/publish`, {}
    );
  }
}
