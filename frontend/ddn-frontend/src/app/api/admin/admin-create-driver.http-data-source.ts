import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

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
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL); 

  createDriver(payload: AdminCreateDriverRequest): Observable<AdminCreateDriverResponse> {
    return this.http.post<AdminCreateDriverResponse>(`${this.baseUrl}/admin/drivers`, payload);
  }
}
