import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

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
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL); 

  listFavorites(userId: number): Observable<FavoriteRouteAnyDto[]> {
    return this.http.get<FavoriteRouteAnyDto[]>(`${this.baseUrl}/users/${userId}/favorite-routes`);
  }

  getFavorite(userId: number, favoriteRouteId: number): Observable<FavoriteRouteAnyDto> {
    return this.http.get<FavoriteRouteAnyDto>(
      `${this.baseUrl}/users/${userId}/favorite-routes/${favoriteRouteId}`
    );
  }

  addFromRide(userId: number, rideId: number): Observable<AddFavoriteFromRideResponseDto> {
    return this.http.post<AddFavoriteFromRideResponseDto>(
      `${this.baseUrl}/users/${userId}/favorite-routes/from-ride/${rideId}`,
      null
    );
  }

  removeFavorite(userId: number, favoriteRouteId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${userId}/favorite-routes/${favoriteRouteId}`);
  }
}

export function isFavoriteRouteNew(x: any): x is FavoriteRouteResponseDto {
  return !!x && !!x.start && typeof x.start.lat === 'number' && typeof x.start.lng === 'number';
}
