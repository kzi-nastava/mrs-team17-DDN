import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

import { API_BASE_URL } from '../../app.config';
import { InconsistencyReport, TrackingState } from '../user/models/ride-tracking.models';
import { RideTrackingDataSource } from '../user/ride-tracking.datasource';

type SubmitInconsistencyRequest = {
  description: string;
};
@Injectable()
export class RideTrackingHttpDataSource implements RideTrackingDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  watchMyActiveTracking(rideId?: number): Observable<TrackingState> {
    const endpoint = rideId != null
      ? `${this.baseUrl}/rides/${rideId}/tracking`
      : `${this.baseUrl}/rides/active/tracking`;
    return this.http.get<TrackingState>(endpoint);
  }

  submitInconsistencyForMyActiveRide(text: string, rideId?: number): Observable<void> {
    const body: SubmitInconsistencyRequest = { description: text };
    const endpoint = rideId != null
      ? `${this.baseUrl}/rides/${rideId}/reports`
      : `${this.baseUrl}/rides/active/reports`;
    return this.http.post<void>(endpoint, body);
  }

  listInconsistenciesForMyActiveRide(_rideId?: number): Observable<InconsistencyReport[]> {
    return of([]);
  }
}
