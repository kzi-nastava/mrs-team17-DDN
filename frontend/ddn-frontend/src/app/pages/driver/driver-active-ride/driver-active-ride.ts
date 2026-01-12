import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RIDE_LIFECYCLE_DS } from './ride-lifecycle.datasource';
import { DriverStateService } from '../../../state/driver-state.service';

@Component({
  selector: 'app-driver-active-ride',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-active-ride.html',
  styleUrl: './driver-active-ride.css',
})
export class DriverActiveRideComponent {
  private ds = inject(RIDE_LIFECYCLE_DS);
  private driverState = inject(DriverStateService);

  rideId = 1;

  pickupLabel = 'Pickup address (mock)';
  destinationLabel = 'Destination address (mock)';
  passengerLabel = 'Passenger (mock)';
  priceLabel = '1200 RSD (mock)';
  statusLabel = 'In progress';

  finished = false;

  finishRide(): void {
    this.ds.finishRide(this.rideId).subscribe(() => {
      this.finished = true;
      this.statusLabel = 'Finished';
      this.driverState.setAvailable(true);
    });
  }
}
