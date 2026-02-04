import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';
import { Notification } from './models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  getMy(limit = 20): Observable<Notification[]> {
    return this.http.get<Notification[]>(
      `${this.baseUrl}/notifications/me?limit=${limit}`
    );
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(
      `${this.baseUrl}/notifications/me/unread-count`
    );
  }

  markRead(id: number): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/notifications/me/${id}/read`,
      {}
    );
  }
}
