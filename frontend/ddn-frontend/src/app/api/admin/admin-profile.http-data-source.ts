import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export interface AdminProfileResponseDto {
  id: number;
  email?: string;
  firstName?: string;
  lastName?: string;
  address?: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  role?: string;
}

export interface UpdateAdminProfileRequestDto {
  firstName?: string;
  lastName?: string;
  address?: string;
  phoneNumber?: string;
  profileImageUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class AdminProfileHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getProfile(adminId: number): Observable<AdminProfileResponseDto> {
    return this.http.get<AdminProfileResponseDto>(`${this.baseUrl}/admins/${adminId}/profile`);
  }

  updateProfile(adminId: number, body: UpdateAdminProfileRequestDto): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/admins/${adminId}/profile`, body);
  }

  uploadProfileImage(adminId: number, file: File): Observable<{ profileImageUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ profileImageUrl: string }>(
      `${this.baseUrl}/admins/${adminId}/profile-image`,
      formData
    );
  }
}
