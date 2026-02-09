import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { API_BASE_URL } from '../../app.config';
import { RideRatingDataSource } from './ride-rating.datasource';
import { RideRatingRequest, RideRatingResponse } from './models/ride-rating.models';

@Injectable()
export class RideRatingHttpDataSource implements RideRatingDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getRating(rideId: number): Observable<RideRatingResponse | null> {
    return this.http
      .get<RideRatingResponse>(`${this.baseUrl}/rides/${rideId}/rating`)
      .pipe(
        catchError((err) => (err?.status === 404 ? of(null) : throwError(() => err)))
      );
  }

  submitRating(rideId: number, body: RideRatingRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/rides/${rideId}/rating`, body);
  }
}
