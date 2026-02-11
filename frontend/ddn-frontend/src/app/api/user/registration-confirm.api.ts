import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RegistrationConfirmApi {

  constructor(private http: HttpClient) {}

  confirm(token: string): Observable<void> {
    return this.http.get<void>(
      '/api/registration/confirm',
      { params: { token } }
    );
  }
}
