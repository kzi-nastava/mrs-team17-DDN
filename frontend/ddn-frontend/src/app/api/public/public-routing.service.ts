import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';

export type GeocodeResult = {
  lat: number;
  lng: number;
  displayName: string;
};

export type RouteResult = {
  distanceKm: number;
  durationMin: number;
  // Leaflet expects LatLng as [lat,lng]
  latLngs: Array<[number, number]>;
};

/**
 * Simple public routing helper for KT2 UI:
 * - Geocode via Nominatim (OpenStreetMap)
 * - Route via OSRM demo server
 *
 * Note: for production you’d proxy these through your backend, but for school KT UI this is fine.
 */
@Injectable({ providedIn: 'root' })
export class PublicRoutingService {
  private http = inject(HttpClient);

  geocode(address: string): Observable<GeocodeResult> {
    const params = new HttpParams()
      .set('format', 'json')
      .set('limit', '1')
      .set('q', address);

    return this.http
      .get<any[]>('https://nominatim.openstreetmap.org/search', { params })
      .pipe(
        map((list) => {
          const hit = list?.[0];
          if (!hit) throw new Error('Address not found');

          return {
            lat: Number(hit.lat),
            lng: Number(hit.lon),
            displayName: String(hit.display_name ?? address),
          } satisfies GeocodeResult;
        })
      );
  }

  route(from: GeocodeResult, to: GeocodeResult): Observable<RouteResult> {
    // OSRM expects lon,lat
    const coords = `${from.lng},${from.lat};${to.lng},${to.lat}`;

    const params = new HttpParams()
      .set('overview', 'full')
      .set('geometries', 'geojson');

    return this.http
      .get<any>(`https://router.project-osrm.org/route/v1/driving/${coords}`, { params })
      .pipe(
        map((resp) => {
          const r = resp?.routes?.[0];
          if (!r?.geometry?.coordinates?.length) throw new Error('Route not available');

          const latLngs: Array<[number, number]> = r.geometry.coordinates.map(
            (c: any) => [Number(c[1]), Number(c[0])]
          );

          return {
            distanceKm: Number(r.distance) / 1000,
            durationMin: Number(r.duration) / 60,
            latLngs,
          } satisfies RouteResult;
        })
      );
  }
}
