import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize, take } from 'rxjs';
import { Router } from '@angular/router';
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
  private router = inject(Router);

  ride: DriverRideDetails | null = null;

  loading = false;
  error: string | null = null;

  finishing = false;
  finished = false;
  postFinishTarget: 'future' | 'home' | null = null;
  postFinishRideId: number | null = null;
  postFinishMessage: string | null = null;

  ngOnInit(): void {
    this.loadActiveRide();
  }

  private loadActiveRide(): void {
    this.loading = true;
    this.error = null;
    this.ride = null;
    this.finished = false;
    this.postFinishTarget = null;
    this.postFinishRideId = null;
    this.postFinishMessage = null;
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
          this.syncAvailabilityFromAcceptedRides();
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
        })
      )
      .subscribe({
        next: () => {
          this.finished = true;
          this.preparePostFinishState();
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

  openFutureRides(): void {
    this.router.navigate(['/driver/future-rides']);
  }

  continueAfterFinish(): void {
    if (this.postFinishTarget === 'future') {
      if (this.postFinishRideId != null) {
        this.router.navigate(['/driver/future-rides'], {
          queryParams: { rideId: this.postFinishRideId },
        });
        return;
      }

      this.router.navigate(['/driver/future-rides']);
      return;
    }

    if (this.postFinishTarget === 'home') {
      this.router.navigate(['/driver/home']);
    }
  }

  private preparePostFinishState(): void {
    this.ridesApi.getAcceptedRides().pipe(take(1)).subscribe({
      next: (rides) => {
        const nextRideId = rides?.length ? rides[0].rideId : null;
        if (nextRideId != null) {
          this.driverState.setAvailable(false);
          this.postFinishTarget = 'future';
          this.postFinishRideId = nextRideId;
          this.postFinishMessage = 'Ride finished. Next assigned ride is ready.';
          return;
        }

        this.driverState.setAvailable(true);
        this.postFinishTarget = 'home';
        this.postFinishRideId = null;
        this.postFinishMessage = 'Ride finished. No assigned future rides.';
      },
      error: () => {
        this.driverState.setAvailable(true);
        this.postFinishTarget = 'home';
        this.postFinishRideId = null;
        this.postFinishMessage = 'Ride finished.';
      },
    });
  }

  private syncAvailabilityFromAcceptedRides(): void {
    this.ridesApi.getAcceptedRides().pipe(take(1)).subscribe({
      next: (rides) => this.driverState.setAvailable(!(rides?.length ?? 0)),
    });
  }
}
