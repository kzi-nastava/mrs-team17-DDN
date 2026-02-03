import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';
import { ChatDataSource } from './chat.datasource';
import { ChatMessageResponse, ChatSendMessageRequest, ChatThreadResponse } from './models/chat.models';

@Injectable()
export class ChatHttpDataSource implements ChatDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  // user
  getMyThread(): Observable<ChatThreadResponse> {
    return this.http.get<ChatThreadResponse>(`${this.baseUrl}/chat/thread/me`);
  }

  getMyMessages(afterId: number | null, limit: number): Observable<ChatMessageResponse[]> {
    let params = new HttpParams().set('limit', String(limit));
    if (afterId != null) params = params.set('afterId', String(afterId));
    return this.http.get<ChatMessageResponse[]>(`${this.baseUrl}/chat/messages/me`, { params });
  }

  sendMyMessage(body: ChatSendMessageRequest): Observable<ChatMessageResponse> {
    return this.http.post<ChatMessageResponse>(`${this.baseUrl}/chat/messages/me`, body);
  }

  // admin
  listThreads(query: string | null, limit: number): Observable<ChatThreadResponse[]> {
    let params = new HttpParams().set('limit', String(limit));
    if (query && query.trim().length > 0) params = params.set('query', query.trim());
    return this.http.get<ChatThreadResponse[]>(`${this.baseUrl}/admin/chats`, { params });
  }

  getThreadMessages(threadId: number, afterId: number | null, limit: number): Observable<ChatMessageResponse[]> {
    let params = new HttpParams().set('limit', String(limit));
    if (afterId != null) params = params.set('afterId', String(afterId));
    return this.http.get<ChatMessageResponse[]>(`${this.baseUrl}/admin/chats/${threadId}/messages`, { params });
  }

  sendAdminMessage(threadId: number, body: ChatSendMessageRequest): Observable<ChatMessageResponse> {
    return this.http.post<ChatMessageResponse>(`${this.baseUrl}/admin/chats/${threadId}/messages`, body);
  }
}
