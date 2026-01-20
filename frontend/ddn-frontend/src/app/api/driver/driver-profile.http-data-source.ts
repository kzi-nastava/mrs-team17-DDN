import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';
import {
  DriverProfileResponseDto,
  ProfileChangeRequestResponseDto,
  UpdateDriverProfileRequestDto,
} from './models/driver-profile.models';

@Injectable({ providedIn: 'root' })
export class DriverProfileHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getProfile(driverId: number): Observable<DriverProfileResponseDto> {
    return this.http.get<DriverProfileResponseDto>(`${this.baseUrl}/drivers/${driverId}/profile`);
  }

  requestProfileChange(
    driverId: number,
    body: UpdateDriverProfileRequestDto
  ): Observable<ProfileChangeRequestResponseDto> {
    return this.http.post<ProfileChangeRequestResponseDto>(
      `${this.baseUrl}/drivers/${driverId}/profile-change-requests`,
      body
    );
  }

  uploadProfileImage(driverId: number, file: File): Observable<{ profileImageUrl: string }> {
  const formData = new FormData();
  formData.append('file', file);

  return this.http.post<{ profileImageUrl: string }>(
    `${this.baseUrl}/drivers/${driverId}/profile-image`,formData);
  }

}
