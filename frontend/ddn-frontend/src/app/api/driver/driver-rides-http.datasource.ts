import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';
import { DriverRideHistoryItem, DriverRideDetails } from './models/driver-rides.models';

@Injectable({ providedIn: 'root' })
export class DriverRidesHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getDriverRides(from?: string | null, to?: string | null): Observable<DriverRideHistoryItem[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);

    return this.http.get<DriverRideHistoryItem[]>(`${this.baseUrl}/driver/rides`, { params });
  }

  getDriverRideDetails(rideId: number): Observable<DriverRideDetails> {
    return this.http.get<DriverRideDetails>(`${this.baseUrl}/driver/rides/${rideId}`);
  }

  getActiveRide(): Observable<DriverRideDetails> {
    return this.http.get<DriverRideDetails>(`${this.baseUrl}/driver/active-ride`);
  }

  getAcceptedRides(): Observable<DriverRideDetails[]> {
    return this.http.get<DriverRideDetails[]>(`${this.baseUrl}/driver/rides/accepted`);
  }

  startRide(rideId: number): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/driver/rides/${rideId}/start`, null);
  }
}
