import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models';

export interface RazorpayOrderResponse {
  razorpayOrderId: string;
  amount: number;
  currency: string;
  keyId: string;
}

export interface VerifyRequest {
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  createOrder(amount: number): Observable<ApiResponse<RazorpayOrderResponse>> {
    return this.http.post<ApiResponse<RazorpayOrderResponse>>(
      `${this.API}/payment/create-order`, { amount, orderId: 0 }
    );
  }

  verifyPayment(req: VerifyRequest): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/payment/verify`, req);
  }
}
