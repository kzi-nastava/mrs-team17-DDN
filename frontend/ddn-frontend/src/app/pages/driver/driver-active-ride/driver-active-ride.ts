import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize, take } from 'rxjs';
import { RIDE_LIFECYCLE_DS } from '../../../api/driver/ride-lifecycle.datasource';
import { DriverStateService } from '../../../state/driver-state.service';
import { DriverRidesHttpDataSource } from '../../../api/driver/driver-rides-http.datasource';
import { DriverRideDetails } from '../../../api/driver/models/driver-rides.models';

@Component({
  selector: 'app-driver-active-ride',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-active-ride.html',
  styleUrl: './driver-active-ride.css',
})
export class DriverActiveRideComponent implements OnInit {
  private lifecycle = inject(RIDE_LIFECYCLE_DS);
  private ridesApi = inject(DriverRidesHttpDataSource);
  private driverState = inject(DriverStateService);

  ride: DriverRideDetails | null = null;

  loading = false;
  error: string | null = null;

  finishing = false;
  finished = false;

  ngOnInit(): void {
    this.loadActiveRide();
  }

  private loadActiveRide(): void {
    this.loading = true;
    this.error = null;
    this.ride = null;
    this.finished = false;
    this.driverState.setAvailable(false);

    this.ridesApi
      .getActiveRide()
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
          this.error = 'No active ride.';
          this.ride = null;
          this.driverState.setAvailable(true);
        },
      });
  }

  finishRide(): void {
    if (!this.ride?.rideId || this.finishing || this.finished) return;

    this.finishing = true;
    this.error = null;

    this.lifecycle
      .finishRide(this.ride.rideId)
      .pipe(
        take(1),
        finalize(() => {
          this.finishing = false;
          this.loadActiveRide();
        })
      )
      .subscribe({
        next: () => {
          this.finished = true;
          this.driverState.setAvailable(true);
        },
        error: () => {
          this.error = 'Finish ride failed.';
        },
      });
  }

  statusText(): string {
    if (!this.ride) return '';
    if (this.finished) return 'Finished';
    return this.ride.canceled ? 'Canceled' : 'In progress';
  }
}
