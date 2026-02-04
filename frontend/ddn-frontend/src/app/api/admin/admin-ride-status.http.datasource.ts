import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export interface AdminRideStatusRowDto {
  rideId: number;
  driverId: number;
  userId: number | null;
  driverEmail: string | null;
  driverFirstName: string | null;
  driverLastName: string | null;
  status: string;
  startedAt: string | null;
  carLat: number;
  carLng: number;
}

@Injectable({ providedIn: 'root' })
export class AdminRideStatusHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  list(q: string, limit = 50): Observable<AdminRideStatusRowDto[]> {
    let params = new HttpParams().set('limit', String(limit));
    if (q && q.trim()) params = params.set('q', q.trim());
    return this.http.get<AdminRideStatusRowDto[]>(`${this.baseUrl}/admin/ride-status`, { params });
  }
}
