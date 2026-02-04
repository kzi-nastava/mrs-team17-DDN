import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export type PendingRatingResponse = { rideId: number };

@Injectable({ providedIn: 'root' })
export class PendingRatingHttpDataSource {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  getPendingRideToRate(): Observable<PendingRatingResponse> {
    return this.http.get<PendingRatingResponse>(`${this.baseUrl}/rides/rate/pending`);
  }
}
