import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface UserRegistrationRequest {
  email: string;
  password: string;
  confirmPassword: string;

  // backend DTO očekuje ova polja:
  firstName: string;
  lastName: string;
  address: string;
  phone: string;
}

@Injectable({ providedIn: 'root' })
export class UserRegistrationApi {
  constructor(private http: HttpClient) {}

  register(payload: UserRegistrationRequest): Observable<any> {
    return this.http.post('/api/registration', payload);
  }
}