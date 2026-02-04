import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../../app.config';

export type ChangePasswordRequestDto = {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
};

@Injectable({ providedIn: 'root' })
export class PasswordChangeHttpDataSource {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  changePassword(req: ChangePasswordRequestDto): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/account/change-password`, req);
  }
}
