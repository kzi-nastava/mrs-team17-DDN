import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import * as L from 'leaflet';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { UserNavbarComponent } from '../../../components/user-navbar/user-navbar';
import { RIDE_TRACKING_DS } from '../../../api/user/ride-tracking.datasource';
import { TrackingState } from '../../../api/user/models/ride-tracking.models';

@Component({
  selector: 'app-ride-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, UserNavbarComponent],
  templateUrl: './ride-tracking.html',
  styleUrl: './ride-tracking.css',
})
export class RideTrackingComponent implements AfterViewInit, OnDestroy {
  private ds = inject(RIDE_TRACKING_DS);
  private route = inject(ActivatedRoute);

  private map!: L.Map;
  private sub: Subscription | null = null;

  private pickupMarker: L.CircleMarker | null = null;
  private destinationMarker: L.CircleMarker | null = null;
  private carMarker: L.CircleMarker | null = null;
  private routeLine: L.Polyline | null = null;

  etaMinutes = 0;
  distanceKm = 0;
  rideStatus = '';

  reportOpen = false;
  reportText = '';

  private rideId!: number;
  private initializedFromState = false;

  ngAfterViewInit(): void {
    const raw = this.route.snapshot.paramMap.get('rideId');
    this.rideId = Number(raw);

    if (!Number.isFinite(this.rideId) || this.rideId <= 0) {
      this.rideStatus = 'Invalid ride';
      return;
    }

    this.initMap();

    this.sub = this.ds.watchTracking(this.rideId).subscribe({
      next: (s) => this.applyState(s),
      error: () => {
        this.rideStatus = 'Tracking not available';
      },
    });

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.sub = null;
    if (this.map) this.map.remove();
  }

  openReport(): void {
    // ✅ only during ACTIVE ride
    if (this.rideStatus !== 'ACTIVE') return;
    this.reportOpen = true;
  }

  closeReport(): void {
    this.reportOpen = false;
    this.reportText = '';
  }

  submitReport(): void {
    // ✅ client-side guard + validation
    if (this.rideStatus !== 'ACTIVE') return;

    const text = this.reportText.trim();
    if (text.length < 5) return;

    this.ds.submitInconsistency(this.rideId, text).subscribe({
      next: () => {
        this.reportOpen = false;
        this.reportText = '';
      },
      error: () => {
        // minimal UX: close modal (or keep it open if you prefer)
        this.reportOpen = false;
      },
    });
  }

  private initMap(): void {
    this.map = L.map('tracking-map', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);
  }

  private applyState(s: TrackingState): void {
    this.etaMinutes = s.etaMinutes;
    this.distanceKm = s.distanceKm;
    this.rideStatus = s.status;

    if (!this.initializedFromState) {
      this.initializedFromState = true;

      this.pickupMarker = L.circleMarker([s.pickup.lat, s.pickup.lng], {
        radius: 8,
        color: '#3498db',
        fillColor: '#3498db',
        fillOpacity: 0.9,
        weight: 2,
      }).addTo(this.map);

      this.destinationMarker = L.circleMarker([s.destination.lat, s.destination.lng], {
        radius: 8,
        color: '#9b59b6',
        fillColor: '#9b59b6',
        fillOpacity: 0.9,
        weight: 2,
      }).addTo(this.map);

      this.carMarker = L.circleMarker([s.car.lat, s.car.lng], {
        radius: 8,
        color: '#2ecc71',
        fillColor: '#2ecc71',
        fillOpacity: 0.9,
        weight: 2,
      }).addTo(this.map);

      this.routeLine = L.polyline(
        [
          [s.car.lat, s.car.lng],
          [s.pickup.lat, s.pickup.lng],
          [s.destination.lat, s.destination.lng],
        ],
        { weight: 3 }
      ).addTo(this.map);

      const bounds = L.latLngBounds([
        [s.car.lat, s.car.lng],
        [s.pickup.lat, s.pickup.lng],
        [s.destination.lat, s.destination.lng],
      ]);
      this.map.fitBounds(bounds, { padding: [30, 30] });

      return;
    }

    this.carMarker?.setLatLng([s.car.lat, s.car.lng]);

    if (this.routeLine) {
      this.routeLine.setLatLngs([
        [s.car.lat, s.car.lng],
        [s.pickup.lat, s.pickup.lng],
        [s.destination.lat, s.destination.lng],
      ]);
    }
  }
}
