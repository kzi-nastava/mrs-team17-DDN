import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { InconsistencyReport, TrackingState } from './models/ride-tracking.models';

export interface RideTrackingDataSource {
  watchMyActiveTracking(): Observable<TrackingState>;
  submitInconsistencyForMyActiveRide(text: string): Observable<void>;
  listInconsistenciesForMyActiveRide(): Observable<InconsistencyReport[]>;
}

export const RIDE_TRACKING_DS = new InjectionToken<RideTrackingDataSource>(
  'RIDE_TRACKING_DS'
);
