import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export interface RidePointRequestDto {
  address: string;
  lat: number;
  lng: number;
}

export interface CreateRideRequestDto {
  requesterUserId: number;
  orderType: 'now' | 'schedule';
  scheduledAt?: string | null;

  start: RidePointRequestDto;
  destination: RidePointRequestDto;
  checkpoints?: RidePointRequestDto[];

  linkedUsers?: string[];

  vehicleType: 'standard' | 'luxury' | 'van';
  babyTransport: boolean;
  petTransport: boolean;
}

export interface CreateRideResponseDto {
  rideId: number;
  driverId: number;
  status: string;
  price: number;
}

@Injectable({ providedIn: 'root' })
export class RideOrderApiService {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  createRide(payload: CreateRideRequestDto): Observable<CreateRideResponseDto> {
    return this.http.post<CreateRideResponseDto>(`${this.baseUrl}/rides`, payload);
  }
}
