import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export interface UserProfileResponseDto {
  id: number;
  email?: string;
  firstName?: string;
  lastName?: string;
  address?: string;
  phoneNumber?: string;
  profileImageUrl?: string;
  role?: string;
  blocked?: boolean;
  blockReason?: string | null;
}

export interface UpdateUserProfileRequestDto {
  firstName?: string;
  lastName?: string;
  address?: string;
  phoneNumber?: string;
  profileImageUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class UserProfileHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getProfile(userId: number): Observable<UserProfileResponseDto> {
    return this.http.get<UserProfileResponseDto>(`${this.baseUrl}/users/${userId}/profile`);
  }

  updateProfile(userId: number, body: UpdateUserProfileRequestDto): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/users/${userId}/profile`, body);
  }

  uploadProfileImage(userId: number, file: File): Observable<{ profileImageUrl: string }> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<{ profileImageUrl: string }>(
      `${this.baseUrl}/users/${userId}/profile-image`,
      formData
    );
  }
}
