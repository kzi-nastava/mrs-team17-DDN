import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import * as L from 'leaflet';
import { finalize, take } from 'rxjs';

import { API_BASE_URL } from '../../../app.config';
import { DriverRidesHttpDataSource } from '../../../api/driver/driver-rides-http.datasource';
import { DriverRideDetails } from '../../../api/driver/models/driver-rides.models';
import { DriverStateService } from '../../../state/driver-state.service';

type LatLng = { lat: number; lng: number };
type RideCheckpoint = {
  stopOrder: number;
  address: string;
  lat: number;
  lng: number;
};

type TrackingState = {
  car: LatLng;
  pickup: LatLng;
  destination: LatLng;
  route: LatLng[];
  checkpoints?: RideCheckpoint[];
  etaMinutes: number;
  distanceKm: number;
  status: string;
};

@Component({
  selector: 'app-driver-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-home.html',
  styleUrl: './driver-home.css',
})
export class DriverHomeComponent implements AfterViewInit, OnDestroy {
  private ridesApi = inject(DriverRidesHttpDataSource);
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);
  private driverState = inject(DriverStateService);
  private router = inject(Router);

  private map!: L.Map;
  private routeLine: L.Polyline | null = null;
  private markerLayers: L.Layer[] = [];

  ride: DriverRideDetails | null = null;
  tracking: TrackingState | null = null;

  loading = false;
  starting = false;
  error: string | null = null;

  ngAfterViewInit(): void {
    this.initMap();

    this.ridesApi.getActiveRide().pipe(take(1)).subscribe({
      next: () => {
        this.driverState.setAvailable(false);
        this.router.navigate(['/driver/active-ride']);
      },
      error: (err) => {
        if (err?.status === 404) {
          this.driverState.setAvailable(true);
          this.loadAcceptedRide();
        } else {
          this.error = 'Unable to check active ride.';
        }
      }
    });
  }

  ngOnDestroy(): void {
    try {
      this.map?.remove();
    } catch {
    }
  }

  private initMap(): void {
    this.map = L.map('startRideMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  private loadAcceptedRide(): void {
    this.loading = true;
    this.error = null;
    this.ride = null;
    this.tracking = null;
    this.clearRouteFromMap();

    this.ridesApi
      .getAcceptedRides()
      .pipe(
        take(1),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (rides) => {
          this.ride = rides && rides.length > 0 ? rides[0] : null;

          if (!this.ride) {
            this.error = 'There are currently no assigned rides.';
            return;
          }

          this.loadTrackingOnce(this.ride.rideId);
        },
        error: () => {
          this.error = 'Unable to load assigned rides.';
        },
      });
  }

  private loadTrackingOnce(rideId: number): void {
    this.http
      .get<TrackingState>(`${this.baseUrl}/rides/${rideId}/tracking`)
      .pipe(take(1))
      .subscribe({
        next: (t) => {
          this.tracking = t ?? null;
          if (this.tracking) this.drawRouteOnMap(this.tracking);
        },
        error: () => {
          this.tracking = null;
        },
      });
  }

  private drawRouteOnMap(t: TrackingState): void {
    if (!this.map) return;

    this.clearRouteFromMap();

    const pts: L.LatLngExpression[] =
      t.route && t.route.length >= 2
        ? t.route.map(p => [p.lat, p.lng] as L.LatLngExpression)
        : [
            [t.pickup.lat, t.pickup.lng] as L.LatLngExpression,
            [t.destination.lat, t.destination.lng] as L.LatLngExpression,
          ];

    this.routeLine = L.polyline(pts, {
      weight: 5,
      opacity: 0.9,
      color: '#28c200',
    }).addTo(this.map);

    const start = pts[0];
    const end = pts[pts.length - 1];

    if (t.checkpoints && t.checkpoints.length > 0) {
      t.checkpoints.forEach(cp => {
        const m = L.circleMarker([cp.lat, cp.lng], {
          radius: 6,
          color: '#ffffff',
          weight: 2,
          fillColor: '#ff9800',
          fillOpacity: 0.95,
        })
          .addTo(this.map)
          .bindPopup(`Stop ${cp.stopOrder}: ${cp.address}`);

        this.markerLayers.push(m);
      });
    }

    const startMarker = L.circleMarker(start, {
      radius: 7,
      color: '#ffffff',
      weight: 2,
      fillColor: '#28c200',
      fillOpacity: 0.95,
    })
      .addTo(this.map)
      .bindPopup('Start');

    const endMarker = L.circleMarker(end, {
      radius: 7,
      color: '#ffffff',
      weight: 2,
      fillColor: '#28c200',
      fillOpacity: 0.95,
    })
      .addTo(this.map)
      .bindPopup('End');

    this.markerLayers.push(startMarker, endMarker);

    this.map.fitBounds(this.routeLine.getBounds(), { padding: [30, 30] });
    setTimeout(() => this.map.invalidateSize(), 0);
  }

  private clearRouteFromMap(): void {
    if (!this.map) return;

    if (this.routeLine) {
      try {
        this.map.removeLayer(this.routeLine);
      } catch {
      }
      this.routeLine = null;
    }

    this.markerLayers.forEach(layer => {
      try {
        this.map.removeLayer(layer);
      } catch {
      }
    });
    this.markerLayers = [];
  }

  canStart(): boolean {
    return !!this.ride && !this.loading && !this.starting;
  }

  startRide(): void {
    if (!this.ride || this.starting) return;

    this.starting = true;
    this.error = null;

    this.ridesApi
      .startRide(this.ride.rideId)
      .pipe(
        take(1),
        finalize(() => (this.starting = false))
      )
      .subscribe({
        next: () => {
          this.driverState.setAvailable(false);
          this.router.navigate(['/driver/active-ride']);
        },
        error: () => {
          this.error = 'The drive start failed. Please try again.';
        },
      });
  }

  refresh(): void {
    if (this.loading || this.starting) return;
    this.loadAcceptedRide();
  }
}
