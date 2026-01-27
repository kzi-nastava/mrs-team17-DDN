import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponse = {
  token: string;
};

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL); // npr. http://localhost:8080/api

  login(req: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, req);
  }
}
