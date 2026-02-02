import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export type ProfileChangeRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface AdminProfileChangeRequestDto {
  requestId: number;
  driverId: number;
  status: ProfileChangeRequestStatus;
  createdAt: string;

  firstName?: string | null;
  lastName?: string | null;
  address?: string | null;
  phoneNumber?: string | null;
  profileImageUrl?: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdminProfileChangeRequestsHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  list(status: ProfileChangeRequestStatus | '' = 'PENDING'): Observable<AdminProfileChangeRequestDto[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<AdminProfileChangeRequestDto[]>(
      `${this.baseUrl}/admin/profile-change-requests`,
      { params }
    );
  }

  get(requestId: number): Observable<AdminProfileChangeRequestDto> {
    return this.http.get<AdminProfileChangeRequestDto>(
      `${this.baseUrl}/admin/profile-change-requests/${requestId}`
    );
  }

  approve(requestId: number, note?: string): Observable<AdminProfileChangeRequestDto> {
    let params = new HttpParams();
    if (note && note.trim()) params = params.set('note', note.trim());
    return this.http.post<AdminProfileChangeRequestDto>(
      `${this.baseUrl}/admin/profile-change-requests/${requestId}/approve`,
      null,
      { params }
    );
  }

  reject(requestId: number, reason?: string): Observable<AdminProfileChangeRequestDto> {
    let params = new HttpParams();
    if (reason && reason.trim()) params = params.set('reason', reason.trim());
    return this.http.post<AdminProfileChangeRequestDto>(
      `${this.baseUrl}/admin/profile-change-requests/${requestId}/reject`,
      null,
      { params }
    );
  }
}
