import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export interface AdminPricingResponseDto {
  standard: number;
  luxury: number;
  van: number;
}

export interface AdminPricingUpdateRequestDto {
  standard: number;
  luxury: number;
  van: number;
}

@Injectable({ providedIn: 'root' })
export class AdminPricingHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  get(): Observable<AdminPricingResponseDto> {
    return this.http.get<AdminPricingResponseDto>(`${this.baseUrl}/admin/pricing`);
  }

  update(body: AdminPricingUpdateRequestDto): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/admin/pricing`, body);
  }
}
