import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DriverStateService {
  private readonly STORAGE_KEY = 'driver_available';

  private availableSubject = new BehaviorSubject<boolean>(
    localStorage.getItem(this.STORAGE_KEY) === 'true'
  );

  available$: Observable<boolean> = this.availableSubject.asObservable();

  setAvailable(value: boolean): void {
    localStorage.setItem(this.STORAGE_KEY, String(value));
    this.availableSubject.next(value);
  }

  getAvailableSnapshot(): boolean {
    return this.availableSubject.value;
  }
}
