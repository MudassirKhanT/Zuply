import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ApiResponse, Product, Category } from '../models';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAll(pincode?: string, name?: string, sortBy?: string): Observable<ApiResponse<Product[]>> {
    let params = new HttpParams();
    if (name)    params = params.set('name', name);
    if (pincode) params = params.set('pincode', pincode);
    if (sortBy)  params = params.set('sortBy', sortBy);

    return this.http.get<ApiResponse<Product[]>>(`${this.API}/products`, { params }).pipe(
      map(res => {
        if (res.success) {
          res.data = res.data.map(p => ({ ...p, category: p.categoryName }));
        }
        return res;
      })
    );
  }

  getById(id: number): Observable<ApiResponse<Product>> {
    return this.http.get<ApiResponse<Product>>(`${this.API}/products/${id}`).pipe(
      map(res => {
        if (res.success) res.data = { ...res.data, category: res.data.categoryName };
        return res;
      })
    );
  }

  delete(id: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.API}/products/${id}`);
  }

  getCategories(): Observable<ApiResponse<Category[]>> {
    return this.http.get<ApiResponse<Category[]>>(`${this.API}/categories`);
  }
}
