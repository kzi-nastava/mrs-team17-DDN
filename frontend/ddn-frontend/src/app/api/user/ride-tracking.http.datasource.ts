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

  watchTracking(rideId: number): Observable<TrackingState> {
    return timer(0, 2000).pipe(
      switchMap(() =>
        this.http.get<TrackingState>(`${this.baseUrl}/rides/${rideId}/tracking`)
      ),
      shareReplay({ bufferSize: 1, refCount: true })
    );
  }

  submitInconsistency(rideId: number, text: string): Observable<void> {
    const body = { description: text };
    return this.http.post<void>(`${this.baseUrl}/rides/${rideId}/reports`, body);
  }

  listInconsistencies(_: number): Observable<InconsistencyReport[]> {
    return of([]);
  }
}
