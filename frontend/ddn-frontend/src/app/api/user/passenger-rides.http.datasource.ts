import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';
import { PassengerRideHistoryItem } from './models/passenger-rides.models';
import { ESortBy } from './models/enums/ESortBy';
import { ESortDirection } from './models/enums/ESortDirection';

@Injectable({ providedIn: 'root' })
export class PassengerRidesHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getMyRideHistory(from?: string | null, to?: string | null, sortBy?: ESortBy, sortDirection? : ESortDirection ): Observable<PassengerRideHistoryItem[]> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to) params = params.set('to', to);
    if (sortBy) params = params.set('sortBy', sortBy);
    if (sortDirection) params = params.set('sortDirection', sortDirection);
    

    return this.http.get<PassengerRideHistoryItem[]>(`${this.baseUrl}/rides/history`, { params });
  }
}
