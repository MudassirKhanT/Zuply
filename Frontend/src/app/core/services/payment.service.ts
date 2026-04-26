import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PaymentOrderRequest, PaymentOrderResponse } from '../models';

@Injectable({ providedIn: 'root' })
export class PaymentService {

  private url = `${environment.apiUrl}/payment`;

  constructor(private http: HttpClient) {}

  createOrder(req: PaymentOrderRequest): Observable<ApiResponse<PaymentOrderResponse>> {
    return this.http.post<ApiResponse<PaymentOrderResponse>>(`${this.url}/create-order`, req);
  }

  verifyPayment(req: { razorpayOrderId: string; razorpayPaymentId: string; razorpaySignature: string }): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.url}/verify`, req);
  }
}
