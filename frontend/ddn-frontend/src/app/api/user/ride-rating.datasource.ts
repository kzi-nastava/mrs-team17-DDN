import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { RideRatingRequest, RideRatingResponse } from './models/ride-rating.models';

export interface RideRatingDataSource {
  getRating(rideId: number): Observable<RideRatingResponse | null>;
  submitRating(rideId: number, body: RideRatingRequest): Observable<void>;
}

export const RIDE_RATING_DS = new InjectionToken<RideRatingDataSource>('RIDE_RATING_DS');
