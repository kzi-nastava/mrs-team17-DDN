import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { DriverRidesHttpDataSource } from '../../../api/driver/driver-rides-http.datasource';
import { DriverRideHistoryItem } from '../../../api/driver/models/driver-rides.models';
import { AuthStore } from '../../../api/auth/auth.store';

@Component({
  selector: 'app-driver-ride-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-ride-history.html',
  styleUrl: './driver-ride-history.css',
})
export class DriverRideHistoryComponent implements OnInit {
  rides: DriverRideHistoryItem[] = [];
  from: string | null = null;
  to: string | null = null;

  loading = false;
  error: string | null = null;

  constructor(
    private router: Router,
    private ridesApi: DriverRidesHttpDataSource,
    private authStore: AuthStore
  ) {}

  ngOnInit(): void {
    const token = this.authStore.getToken();
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    const role = this.authStore.getRoleFromToken(token);
    if (role !== 'DRIVER') {
      this.router.navigate(['/login']);
      return;
    }

    this.loadRides();
  }

  onFromChange(ev: Event): void {
    const v = (ev.target as HTMLInputElement).value;
    this.from = v ? v : null;
  }

  onToChange(ev: Event): void {
    const v = (ev.target as HTMLInputElement).value;
    this.to = v ? v : null;
  }

  applyFilter(): void {
    if (this.from && this.to && this.from > this.to) {
      this.error = 'From date must be before To date.';
      return;
    }
    this.loadRides();
  }

  private loadRides(): void {
    this.loading = true;
    this.error = null;

    this.ridesApi
      .getDriverRides(this.from, this.to)
      .pipe(
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (data: DriverRideHistoryItem[]) => {
          this.rides = Array.isArray(data) ? data : [];
        },
        error: () => {
          this.error = 'Failed to load ride history.';
          this.rides = [];
        },
      });
  }

  openRideDetails(rideId: number): void {
    this.router.navigate(['/driver/ride-details', rideId]);
  }

  formatDateOnly(iso: string): string {
    if (!iso) return '';
    const y = iso.slice(0, 4);
    const m = iso.slice(5, 7);
    const d = iso.slice(8, 10);
    if (y.length === 4 && m.length === 2 && d.length === 2) return `${d}.${m}.${y}`;
    const dt = new Date(iso);
    const dd = String(dt.getDate()).padStart(2, '0');
    const mm = String(dt.getMonth() + 1).padStart(2, '0');
    const yyyy = dt.getFullYear();
    return `${dd}.${mm}.${yyyy}`;
  }

  formatStatus(status: string | undefined, canceled: boolean): string {
    if (canceled) return 'Canceled';
    const s = (status || '').toUpperCase();
    if (s === 'COMPLETED') return 'Completed';
    if (s === 'CANCELED') return 'Canceled';
    if (s === 'ACTIVE') return 'Active';
    return status || 'Unknown';
  }

  isCompleted(status: string | undefined, canceled: boolean): boolean {
    return !canceled && (status || '').toUpperCase() === 'COMPLETED';
  }

  isCanceled(status: string | undefined, canceled: boolean): boolean {
    return canceled || (status || '').toUpperCase() === 'CANCELED';
  }
}
