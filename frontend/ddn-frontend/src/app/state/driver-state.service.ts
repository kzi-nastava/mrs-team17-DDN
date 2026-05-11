// src/app/state/driver-state.service.ts

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { BehaviorSubject, Observable } from 'rxjs';
import { API_BASE_URL } from '../app.config';
import { TrackingState } from '../api/user/models/ride-tracking.models';


@Injectable({
  providedIn: 'root'
})
export class DriverStateService {

  private http = inject(HttpClient);

  private readonly baseUrl =
    inject(API_BASE_URL);

  private readonly AVAILABLE_KEY =
    'driver_available';

  private readonly DRIVER_ID_KEY =
    'driver_id';

  // =========================
  // AVAILABLE
  // =========================

  private availableSubject =
    new BehaviorSubject<boolean>(
      localStorage.getItem(
        this.AVAILABLE_KEY
      ) === 'true'
    );

  available$: Observable<boolean> =
    this.availableSubject.asObservable();

  // =========================
  // DRIVER ID
  // =========================

  private driverIdSubject =
    new BehaviorSubject<number | null>(
      this.readDriverIdFromStorage()
    );

  driverId$: Observable<number | null> =
    this.driverIdSubject.asObservable();

  // =========================
  // TRACKING STATE
  // =========================

  private trackingStateSubject =
    new BehaviorSubject<TrackingState | null>(
      null
    );

  trackingState$ =
    this.trackingStateSubject.asObservable();

  constructor() {}

  // =========================
  // AVAILABLE
  // =========================

  setAvailable(value: boolean): void {

    localStorage.setItem(
      this.AVAILABLE_KEY,
      String(value)
    );

    this.availableSubject.next(value);
  }

  getAvailableSnapshot(): boolean {
    return this.availableSubject.value;
  }

  // =========================
  // DRIVER ID
  // =========================

  setDriverId(id: number | null): void {

    if (id == null) {

      localStorage.removeItem(
        this.DRIVER_ID_KEY
      );

      this.driverIdSubject.next(null);

      return;
    }

    localStorage.setItem(
      this.DRIVER_ID_KEY,
      String(id)
    );

    this.driverIdSubject.next(id);
  }

  getDriverIdSnapshot(): number | null {
    return this.driverIdSubject.value;
  }

  private readDriverIdFromStorage():
    number | null {

    const raw =
      localStorage.getItem(
        this.DRIVER_ID_KEY
      );

    if (!raw) return null;

    const n = Number(raw);

    return Number.isFinite(n) && n > 0
      ? n
      : null;
  }

  // =========================
  // TRACKING STATE
  // =========================

  setTrackingState(
    value: TrackingState | null
  ): void {

    this.trackingStateSubject.next(value);

    console.log(value);
  }

  getTrackingStateSnapshot():
    TrackingState | null {

    return this.trackingStateSubject.value;
  }

  clearTrackingState(): void {

    this.trackingStateSubject.next(null);
  }

  // =========================
  // LOAD ACTIVE RIDE
  // =========================

  loadActiveRideTracking(): void {

    const endpoint =
      `${this.baseUrl}/rides/active-ride/driver`;
      console.log(endpoint)

    this.http
      .get<TrackingState>(endpoint)
      .subscribe({

        next: (tracking) => {

          this.setTrackingState(
            tracking
          );

          console.log(
            'TRACKING LOADED'
          );

          console.log(tracking);
        },

        error: (err) => {

          console.error(
            'Failed to load tracking',
            err
          );
        }
      });
  }
}