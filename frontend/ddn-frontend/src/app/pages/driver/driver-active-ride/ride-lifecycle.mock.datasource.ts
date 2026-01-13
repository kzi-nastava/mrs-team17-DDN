import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { RideLifecycleDataSource } from './ride-lifecycle.datasource';

@Injectable()
export class RideLifecycleMockDataSource implements RideLifecycleDataSource {
  finishRide(rideId: number): Observable<void> {
    localStorage.setItem(`ride_finished_${rideId}`, 'true');
    localStorage.setItem(`ride_finished_at_${rideId}`, new Date().toISOString());
    return of(void 0);
  }
}
