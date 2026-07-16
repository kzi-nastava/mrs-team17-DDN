import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

import { RidePassengerDetails } from './models/ride-passenger-details.models';

// Fetches everything needed for the ride-history "Info" popup:
// route/map, checkpoints, reports, rating and driver info in one call.
// Ride creation (repeat ride) is handled separately by RideOrderApiService
// (see ride-order.http-data-source.ts) - no need to duplicate that here.
@Injectable({ providedIn: 'root' })
export class RidePassengerDetailsHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getRideDetails(rideId: number): Observable<RidePassengerDetails> {
    return this.http.get<RidePassengerDetails>(`${this.baseUrl}/rides/${rideId}/passenger-details`);
  }
}
