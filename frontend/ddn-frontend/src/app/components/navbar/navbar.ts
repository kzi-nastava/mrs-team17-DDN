import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { DriverStateService } from '../../state/driver-state.service';
import { AuthStore } from '../../api/auth/auth.store';
import { TrackingState } from '../../api/user/models/ride-tracking.models';


@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
})
export class NavbarComponent implements OnInit {

  private driverState =
    inject(DriverStateService);

  private auth =
    inject(AuthStore);

  private router =
    inject(Router);

  driverAvailable$ =
    this.driverState.available$;

  // OVDE ČUVAŠ RESPONSE
  trackingResponse:
    TrackingState | null = null;

  ngOnInit(): void {

    // poziv backend-a
    this.driverState
      .loadActiveRideTracking();

    // uzimanje response-a iz state-a
    this.driverState
      .trackingState$
      .subscribe({

        next: (response) => {

          this.trackingResponse =
            response;

          console.log(
            'TRACKING RESPONSE'
          );

          console.log(response);
        }
      });
  }

  logout(): void {

    // prvo očisti auth
    this.auth.clear();

    // pa state
    this.driverState.setDriverId(null);

    this.driverState.setStatus(false);

    this.driverState.clearTrackingState();

    console.log(
      this.driverState.getTrackingStateSnapshot()
    );

    // tek onda navigate
    this.router.navigate(['/login']);
  }
  toggleAvailability(): void {

    const current =
      this.driverState.getAvailableSnapshot();

    const newValue =
      !current;

    this.driverState.setStatus(newValue);
  }
}

