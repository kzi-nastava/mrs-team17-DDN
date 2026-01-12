import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { InconsistencyReport, TrackingState } from './ride-tracking.models';

export interface RideTrackingDataSource {
  watchTracking(rideId: number): Observable<TrackingState>;
  submitInconsistency(rideId: number, text: string): Observable<void>;
  listInconsistencies(rideId: number): Observable<InconsistencyReport[]>;
}

export const RIDE_TRACKING_DS = new InjectionToken<RideTrackingDataSource>(
  'RIDE_TRACKING_DS'
);
