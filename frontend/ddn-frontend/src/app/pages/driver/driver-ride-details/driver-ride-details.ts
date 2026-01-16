import { Component, OnInit } from '@angular/core';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { finalize, take } from 'rxjs';
import { DriverRidesHttpDataSource } from '../../../api/driver/driver-rides-http.datasource';
import { DriverRideDetails } from '../../../api/driver/models/driver-rides.models';

@Component({
  selector: 'app-driver-ride-details',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './driver-ride-details.html',
  styleUrl: './driver-ride-details.css',
})
export class DriverRideDetailsComponent implements OnInit {
  ride: DriverRideDetails | null = null;
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private ridesApi: DriverRidesHttpDataSource
  ) {}

  ngOnInit(): void {
    const idParam =
      this.route.snapshot.paramMap.get('rideId') ??
      this.route.snapshot.paramMap.get('id');

    const rideId = idParam ? Number(idParam) : NaN;

    if (!Number.isFinite(rideId) || rideId <= 0) {
      this.error = 'Invalid ride id.';
      return;
    }

    this.loadRide(rideId);
  }

  private loadRide(rideId: number): void {
    this.loading = true;
    this.error = null;
    this.ride = null;

    this.ridesApi
      .getDriverRideDetails(rideId)
      .pipe(
        take(1),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (data) => {
          this.ride = data ?? null;
        },
        error: () => {
          this.error = 'Failed to load ride details.';
          this.ride = null;
        },
      });
  }

  formatDateTime(iso: string): string {
    if (!iso) return '';
    const y = iso.slice(0, 4);
    const m = iso.slice(5, 7);
    const d = iso.slice(8, 10);
    const hh = iso.slice(11, 13);
    const mins = iso.slice(14, 16);
    if (
      y.length === 4 &&
      m.length === 2 &&
      d.length === 2 &&
      hh.length === 2 &&
      mins.length === 2
    ) {
      return `${d}.${m}.${y} ${hh}:${mins}`;
    }
    const dt = new Date(iso);
    const dd = String(dt.getDate()).padStart(2, '0');
    const mm = String(dt.getMonth() + 1).padStart(2, '0');
    const yyyy = dt.getFullYear();
    const h = String(dt.getHours()).padStart(2, '0');
    const mi = String(dt.getMinutes()).padStart(2, '0');
    return `${dd}.${mm}.${yyyy} ${h}:${mi}`;
  }

  statusText(): string {
    if (!this.ride) return '';
    if (this.ride.canceled) return 'Canceled';

    const status = String((this.ride as any).status ?? '').toUpperCase();
    if (status === 'ACTIVE') return 'Active';
    if (status === 'FINISHED') return 'Finished';
    if (status === 'COMPLETED') return 'Finished';

    return 'Finished';
  }

  panicText(): string {
    if (!this.ride) return '';
    return this.ride.panicTriggered ? 'Yes' : 'No';
  }
}
