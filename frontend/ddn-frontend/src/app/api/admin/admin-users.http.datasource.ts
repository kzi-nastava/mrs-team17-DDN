import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';
import { AdminUserOptionDto } from './models/admin-user-option.model';
import { AdminSetUserBlockRequestDto, AdminUserStatusDto } from './models/admin-user-status.model';

@Injectable({ providedIn: 'root' })
export class AdminUsersHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  listUsers(role: 'DRIVER' | 'PASSENGER', query: string, limit = 200): Observable<AdminUserOptionDto[]> {
    let params = new HttpParams().set('role', role).set('limit', String(limit));
    const q = (query || '').trim();
    if (q) params = params.set('query', q);

    return this.http.get<AdminUserOptionDto[]>(`${this.baseUrl}/admin/users`, { params });
  }

  listUsersWithStatus(role: 'DRIVER' | 'PASSENGER', query: string, limit = 200): Observable<AdminUserStatusDto[]> {
    let params = new HttpParams().set('role', role).set('limit', String(limit));
    const q = (query || '').trim();
    if (q) params = params.set('query', q);

    return this.http.get<AdminUserStatusDto[]>(`${this.baseUrl}/admin/users/status`, { params });
  }

  setBlockStatus(userId: number, body: AdminSetUserBlockRequestDto): Observable<AdminUserStatusDto> {
    const payload: AdminSetUserBlockRequestDto = {
      blocked: !!body.blocked,
      blockReason: (body.blockReason ?? '').trim() ? (body.blockReason ?? '').trim() : null,
    };

    return this.http.put<AdminUserStatusDto>(`${this.baseUrl}/admin/users/${userId}/block`, payload);
  }
}
