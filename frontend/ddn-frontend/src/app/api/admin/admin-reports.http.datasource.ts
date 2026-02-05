import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';
import { RideStatsReportResponseDto } from '../user/models/reports.models';

@Injectable({ providedIn: 'root' })
export class AdminReportsHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getAdminRideReport(
    role: 'DRIVER' | 'PASSENGER',
    from: string,
    to: string,
    userId?: number
  ): Observable<RideStatsReportResponseDto> {
    let params = new HttpParams().set('role', role).set('from', from).set('to', to);
    if (userId !== undefined && userId !== null) params = params.set('userId', String(userId));
    return this.http.get<RideStatsReportResponseDto>(`${this.baseUrl}/admin/reports/rides`, { params });
  }
}
