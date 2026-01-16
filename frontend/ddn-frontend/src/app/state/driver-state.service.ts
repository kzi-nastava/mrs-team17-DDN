// src/app/state/driver-state.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DriverStateService {
  private readonly AVAILABLE_KEY = 'driver_available';
  private readonly DRIVER_ID_KEY = 'driver_id';

  private availableSubject = new BehaviorSubject<boolean>(
    localStorage.getItem(this.AVAILABLE_KEY) === 'true'
  );
  available$: Observable<boolean> = this.availableSubject.asObservable();

  private driverIdSubject = new BehaviorSubject<number | null>(this.readDriverIdFromStorage());
  driverId$: Observable<number | null> = this.driverIdSubject.asObservable();

  setAvailable(value: boolean): void {
    localStorage.setItem(this.AVAILABLE_KEY, String(value));
    this.availableSubject.next(value);
  }

  getAvailableSnapshot(): boolean {
    return this.availableSubject.value;
  }

  setDriverId(id: number | null): void {
    if (id == null) {
      localStorage.removeItem(this.DRIVER_ID_KEY);
      this.driverIdSubject.next(null);
      return;
    }
    localStorage.setItem(this.DRIVER_ID_KEY, String(id));
    this.driverIdSubject.next(id);
  }

  getDriverIdSnapshot(): number | null {
    return this.driverIdSubject.value;
  }

  private readDriverIdFromStorage(): number | null {
    const raw = localStorage.getItem(this.DRIVER_ID_KEY);
    if (!raw) return null;
    const n = Number(raw);
    return Number.isFinite(n) && n > 0 ? n : null;
  }
}
