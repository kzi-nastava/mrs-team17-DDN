import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  createRide(payload: CreateRideRequestDto): Observable<CreateRideResponseDto> {
    return this.http.post<CreateRideResponseDto>(`${this.baseUrl}/api/rides`, payload);
  }
}
