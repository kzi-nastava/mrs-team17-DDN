import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import * as L from 'leaflet';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { UserNavbarComponent } from '../../../components/user-navbar/user-navbar';
import { RIDE_TRACKING_DS } from './ride-tracking.datasource';
import { TrackingState } from './ride-tracking.models';

@Component({
  selector: 'app-ride-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, UserNavbarComponent],
  templateUrl: './ride-tracking.html',
  styleUrl: './ride-tracking.css',
})
export class RideTrackingComponent implements AfterViewInit, OnDestroy {
  private ds = inject(RIDE_TRACKING_DS);

  private map!: L.Map;
  private carMarker!: L.CircleMarker;
  private sub: Subscription | null = null;

  etaMinutes = 7;
  distanceKm = 3.4;
  rideStatus = 'Driver approaching';

  pickupLabel = 'Pickup address (mock)';
  destinationLabel = 'Destination address (mock)';
  vehicleLabel = 'Audi A6 (mock)';
  driverLabel = 'Driver Name (mock)';

  reportOpen = false;
  reportText = '';

  private rideId = 1;
  private lastState: TrackingState | null = null;

  ngAfterViewInit(): void {
    this.initMap();

    this.sub = this.ds.watchTracking(this.rideId).subscribe((s) => {
      this.lastState = s;
      this.etaMinutes = s.etaMinutes;
      this.distanceKm = s.distanceKm;
      this.rideStatus = s.status;
      this.carMarker.setLatLng([s.car.lat, s.car.lng]);
    });

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.sub = null;
    if (this.map) this.map.remove();
  }

  openReport(): void {
    this.reportOpen = true;
  }

  closeReport(): void {
    this.reportOpen = false;
    this.reportText = '';
  }

  submitReport(): void {
    this.ds.submitInconsistency(this.rideId, this.reportText).subscribe(() => {
      this.reportOpen = false;
      this.reportText = '';
    });
  }

  private initMap(): void {
    const pickup = { lat: 45.2671, lng: 19.8335 };
    const destination = { lat: 45.2558, lng: 19.8452 };
    const car = { lat: 45.2692, lng: 19.8298 };

    this.map = L.map('tracking-map', {
      center: [pickup.lat, pickup.lng],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    L.circleMarker([pickup.lat, pickup.lng], {
      radius: 8,
      color: '#3498db',
      fillColor: '#3498db',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);

    L.circleMarker([destination.lat, destination.lng], {
      radius: 8,
      color: '#9b59b6',
      fillColor: '#9b59b6',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);

    this.carMarker = L.circleMarker([car.lat, car.lng], {
      radius: 8,
      color: '#2ecc71',
      fillColor: '#2ecc71',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);

    L.polyline(
      [
        [car.lat, car.lng],
        [pickup.lat, pickup.lng],
        [destination.lat, destination.lng],
      ],
      { weight: 3 }
    ).addTo(this.map);

    const bounds = L.latLngBounds([
      [car.lat, car.lng],
      [pickup.lat, pickup.lng],
      [destination.lat, destination.lng],
    ]);
    this.map.fitBounds(bounds, { padding: [30, 30] });
  }
}
