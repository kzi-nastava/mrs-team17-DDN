import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../app.config';

export type ActiveVehicleDto = {
  id: number;
  latitude: number;
  longitude: number;
  busy: boolean;
};

@Injectable({ providedIn: 'root' })
export class ActiveVehiclesHttpDataSource {
  private http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getActiveVehicles(): Observable<ActiveVehicleDto[]> {
    return this.http.get<ActiveVehicleDto[]>(`${this.baseUrl}/vehicles/active`, {
      params: { ts: Date.now().toString() },
    });
  }
}
