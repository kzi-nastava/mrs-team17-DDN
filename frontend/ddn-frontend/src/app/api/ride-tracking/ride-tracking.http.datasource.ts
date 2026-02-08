import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';
import { InconsistencyReport, TrackingState } from '../user/models/ride-tracking.models';
import { RideTrackingDataSource } from '../user/ride-tracking.datasource';

type SubmitInconsistencyRequest = {
  text: string;
};
@Injectable()
export class RideTrackingHttpDataSource implements RideTrackingDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  watchMyActiveTracking(): Observable<TrackingState> {
    return this.http.get<TrackingState>(`${this.baseUrl}/rides/active/tracking`);
  }

  submitInconsistencyForMyActiveRide(text: string): Observable<void> {
    const body: SubmitInconsistencyRequest = { text };
    return this.http.post<void>(`${this.baseUrl}/rides/active/reports`, body);
  }

  listInconsistenciesForMyActiveRide(): Observable<InconsistencyReport[]> {
    return this.http.get<InconsistencyReport[]>(`${this.baseUrl}/rides/active/reports`);
  }
}
