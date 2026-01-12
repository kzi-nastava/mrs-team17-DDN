import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';

export interface RideLifecycleDataSource {
  finishRide(rideId: number): Observable<void>;
  resetMock?(rideId: number): Observable<void>;
}

export const RIDE_LIFECYCLE_DS = new InjectionToken<RideLifecycleDataSource>(
  'RIDE_LIFECYCLE_DS'
);
