import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, throwError } from 'rxjs';

/** Novi backend format (po dogovoru): start/destination/stops su objekti sa address/lat/lng */
export interface FavoriteRoutePointResponseDto {
  address: string;
  lat: number;
  lng: number;
}

export interface FavoriteRouteResponseDto {
  id: number;
  name: string;
  start: FavoriteRoutePointResponseDto;
  destination: FavoriteRoutePointResponseDto;
  stops: FavoriteRoutePointResponseDto[];
}

/** Fallback za stari format (ako ti endpoint još vraća stringove) */
export interface FavoriteRouteLegacyResponseDto {
  id: number;
  startAddress: string;
  destinationAddress: string;
  stops: string[];
  name?: string;
}

export type FavoriteRouteAnyDto = FavoriteRouteResponseDto | FavoriteRouteLegacyResponseDto;

@Injectable({ providedIn: 'root' })
export class FavoriteRoutesApiService {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  listFavorites(userId: number): Observable<FavoriteRouteAnyDto[]> {
    return this.http.get<FavoriteRouteAnyDto[]>(
      `${this.baseUrl}/api/users/${userId}/favorite-routes`
    );
  }

  getFavorite(userId: number, favoriteRouteId: number): Observable<FavoriteRouteAnyDto> {
    return this.http
      .get<FavoriteRouteAnyDto>(`${this.baseUrl}/api/users/${userId}/favorite-routes/${favoriteRouteId}`)
      .pipe(
        catchError(() =>
          this.listFavorites(userId).pipe(
            map(list => {
              const found = (list || []).find(x => Number((x as any).id) === Number(favoriteRouteId));
              if (!found) throw new Error('Favourite route not found.');
              return found;
            })
          )
        )
      );
  }

  removeFavorite(userId: number, favoriteRouteId: number) {
  return this.http.delete<void>(`${this.baseUrl}/api/users/${userId}/favorite-routes/${favoriteRouteId}`);
}

}

export function isFavoriteRouteNew(x: any): x is FavoriteRouteResponseDto {
  return !!x && !!x.start && typeof x.start.lat === 'number' && typeof x.start.lng === 'number';
}
