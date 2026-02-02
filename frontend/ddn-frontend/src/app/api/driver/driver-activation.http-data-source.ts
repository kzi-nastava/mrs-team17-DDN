import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';

export type DriverActivateAccountRequest = {
  token: string;
  password: string;
  confirmPassword: string;
};

@Injectable({ providedIn: 'root' })
export class DriverActivationApi {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  activate(req: DriverActivateAccountRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/drivers/activation`, req);
  }
}
