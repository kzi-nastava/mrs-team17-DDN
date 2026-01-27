import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AdminCreateDriverRequest {
  email: string;
  firstName: string;
  lastName: string;
  address: string;
  phoneNumber: string;

  vehicleModel: string;
  vehicleType: 'standard' | 'luxury' | 'van';
  licensePlate: string;
  seats: number;

  babyTransport: boolean;
  petTransport: boolean;
}

export interface AdminCreateDriverResponse {
  driverId: number;
  email: string;
  status: string;
  activationLinkValidHours: number;
}

@Injectable({ providedIn: 'root' })
export class AdminDriverApiService {
  private readonly baseUrl = 'http://localhost:8080';

  constructor(private http: HttpClient) {}

  createDriver(payload: AdminCreateDriverRequest): Observable<AdminCreateDriverResponse> {
    return this.http.post<AdminCreateDriverResponse>(
      `${this.baseUrl}/api/admin/drivers`,
      payload
    );
  }
}
