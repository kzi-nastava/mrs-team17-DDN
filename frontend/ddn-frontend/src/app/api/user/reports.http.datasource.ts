import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';
import { RideStatsReportResponseDto } from './models/reports.models';

@Injectable({ providedIn: 'root' })
export class UserReportsHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getMyRideReport(from: string, to: string): Observable<RideStatsReportResponseDto> {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<RideStatsReportResponseDto>(`${this.baseUrl}/reports/rides`, { params });
  }
}
