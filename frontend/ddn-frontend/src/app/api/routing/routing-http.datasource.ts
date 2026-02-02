import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export type LatLng = { lat: number; lng: number };

export type RoutePreviewResponse = {
  route: LatLng[];
  etaMinutes: number;
  distanceKm: number;
};

@Injectable({ providedIn: 'root' })
export class RoutingHttpDataSource {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  previewRoute(points: LatLng[]): Observable<RoutePreviewResponse> {
    return this.http.post<RoutePreviewResponse>(`${this.baseUrl}/routing/route`, { points });
  }
}
