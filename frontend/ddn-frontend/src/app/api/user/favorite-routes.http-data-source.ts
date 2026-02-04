import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map } from 'rxjs';

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

export interface FavoriteRouteLegacyResponseDto {
  id: number;
  startAddress: string;
  destinationAddress: string;
  stops: string[];
  name?: string;
}

export type FavoriteRouteAnyDto = FavoriteRouteResponseDto | FavoriteRouteLegacyResponseDto;

export interface AddFavoriteFromRideResponseDto {
  favoriteRouteId: number;
  status: string;
}

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
      .get<FavoriteRouteAnyDto>(
        `${this.baseUrl}/api/users/${userId}/favorite-routes/${favoriteRouteId}`
      )
      .pipe(
        catchError(() =>
          this.listFavorites(userId).pipe(
            map(list => {
              const found = (list || []).find(
                x => Number((x as any).id) === Number(favoriteRouteId)
              );
              if (!found) throw new Error('Favourite route not found.');
              return found;
            })
          )
        )
      );
  }

  addFromRide(userId: number, rideId: number): Observable<AddFavoriteFromRideResponseDto> {
    return this.http.post<AddFavoriteFromRideResponseDto>(
      `${this.baseUrl}/api/users/${userId}/favorite-routes/from-ride/${rideId}`,
      null
    );
  }

  removeFavorite(userId: number, favoriteRouteId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/api/users/${userId}/favorite-routes/${favoriteRouteId}`
    );
  }
}

export function isFavoriteRouteNew(x: any): x is FavoriteRouteResponseDto {
  return !!x && !!x.start && typeof x.start.lat === 'number' && typeof x.start.lng === 'number';
}
