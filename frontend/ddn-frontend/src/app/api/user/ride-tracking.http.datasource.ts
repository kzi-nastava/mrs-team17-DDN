import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, timer } from 'rxjs';
import { switchMap, shareReplay } from 'rxjs/operators';
import { RideTrackingDataSource } from './ride-tracking.datasource';
import { InconsistencyReport, TrackingState } from './models/ride-tracking.models';
import { API_BASE_URL } from '../../app.config';

@Injectable()
export class RideTrackingHttpDataSource implements RideTrackingDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  watchMyActiveTracking(rideId?: number): Observable<TrackingState> {
    const endpoint = rideId != null
      ? `${this.baseUrl}/rides/${rideId}/tracking`
      : `${this.baseUrl}/rides/active/tracking`;

    return timer(0, 2000).pipe(
      switchMap(() =>
        this.http.get<TrackingState>(endpoint)
      ),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  submitInconsistencyForMyActiveRide(text: string, rideId?: number): Observable<void> {
    const body = { description: text };
    const endpoint = rideId != null
      ? `${this.baseUrl}/rides/${rideId}/reports`
      : `${this.baseUrl}/rides/active/reports`;
    return this.http.post<void>(endpoint, body);
  }

  listInconsistenciesForMyActiveRide(_rideId?: number): Observable<InconsistencyReport[]> {
    return of([]);
  }
}
