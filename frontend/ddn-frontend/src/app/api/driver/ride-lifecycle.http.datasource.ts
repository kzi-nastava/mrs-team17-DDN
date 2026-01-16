import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RideLifecycleDataSource } from './ride-lifecycle.datasource';
import { API_BASE_URL } from '../../app.config';

@Injectable()
export class RideLifecycleHttpDataSource implements RideLifecycleDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  finishRide(rideId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/rides/${rideId}/finish`, null);
  }
}
