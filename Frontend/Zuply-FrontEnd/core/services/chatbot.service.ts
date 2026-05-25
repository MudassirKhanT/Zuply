import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models';

export interface ChatTurn {
  role: 'user' | 'model';
  text: string;
}

export interface ChatReply {
  reply: string;
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {

  private readonly API = environment.apiUrl;

  constructor(private http: HttpClient) {}

  send(message: string, history: ChatTurn[]): Observable<ApiResponse<ChatReply>> {
    return this.http.post<ApiResponse<ChatReply>>(`${this.API}/chat`, { message, history });
  }
}
