import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, Product, Category } from '../models';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private url    = `${environment.apiUrl}/products`;
  private catUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) {}

  getAll(pincode?: string, name?: string, sortBy?: string): Observable<ApiResponse<Product[]>> {
    let params = new HttpParams();
    if (pincode) params = params.set('pincode', pincode);
    if (name)    params = params.set('name', name);
    if (sortBy)  params = params.set('sortBy', sortBy);
    return this.http.get<ApiResponse<Product[]>>(this.url, { params });
  }

  getById(id: number): Observable<ApiResponse<Product>> {
    return this.http.get<ApiResponse<Product>>(`${this.url}/${id}`);
  }

  getCategories(): Observable<ApiResponse<Category[]>> {
    return this.http.get<ApiResponse<Category[]>>(this.catUrl);
  }

  create(product: Partial<Product>): Observable<ApiResponse<Product>> {
    return this.http.post<ApiResponse<Product>>(this.url, product);
  }

  update(id: number, product: Partial<Product>): Observable<ApiResponse<Product>> {
    return this.http.put<ApiResponse<Product>>(`${this.url}/${id}`, product);
  }

  delete(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.url}/${id}`);
  }

  getMyProducts(): Observable<ApiResponse<Product[]>> {
    return this.http.get<ApiResponse<Product[]>>(`${environment.apiUrl}/seller/products`);
  }
}
