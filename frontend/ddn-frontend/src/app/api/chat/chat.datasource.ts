import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatMessageResponse, ChatSendMessageRequest, ChatThreadResponse } from './models/chat.models';

export interface ChatDataSource {
  // user
  getMyThread(): Observable<ChatThreadResponse>;
  getMyMessages(afterId: number | null, limit: number): Observable<ChatMessageResponse[]>;
  sendMyMessage(body: ChatSendMessageRequest): Observable<ChatMessageResponse>;

  // admin
  listThreads(query: string | null, limit: number): Observable<ChatThreadResponse[]>;
  getThreadMessages(threadId: number, afterId: number | null, limit: number): Observable<ChatMessageResponse[]>;
  sendAdminMessage(threadId: number, body: ChatSendMessageRequest): Observable<ChatMessageResponse>;
}

export const CHAT_DS = new InjectionToken<ChatDataSource>('CHAT_DS');
