import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
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
  selector: 'app-driver-future-rides',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-future-rides.html',
  styleUrl: './driver-future-rides.css',
})
export class DriverFutureRidesComponent implements AfterViewInit, OnDestroy {
  private ridesApi = inject(DriverRidesHttpDataSource);
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);
  private driverState = inject(DriverStateService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  private map!: L.Map;
  private routeLine: L.Polyline | null = null;
  private markerLayers: L.Layer[] = [];

  acceptedRides: DriverRideDetails[] = [];
  ride: DriverRideDetails | null = null;
  tracking: TrackingState | null = null;
  private preferredRideId: number | null = null;

  loading = false;
  starting = false;
  error: string | null = null;

  ngAfterViewInit(): void {
    this.initMap();
    this.preferredRideId = this.parseRideId(this.route.snapshot.queryParamMap.get('rideId'));

    this.ridesApi.getActiveRide().pipe(take(1)).subscribe({
      next: () => {
        this.driverState.setAvailable(false);
        this.router.navigate(['/driver/active-ride']);
      },
      error: (err) => {
        if (err?.status === 404) {
          this.loadAcceptedRides();
        } else {
          this.error = 'Unable to check active ride.';
        }
      },
    });
  }

  ngOnDestroy(): void {
    try {
      this.map?.remove();
    } catch {
    }
  }

  private initMap(): void {
    this.map = L.map('futureRideMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  private loadAcceptedRides(): void {
    this.loading = true;
    this.error = null;
    this.acceptedRides = [];
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
          this.acceptedRides = Array.isArray(rides) ? rides : [];
          if (this.acceptedRides.length === 0) {
            this.error = 'There are currently no assigned rides.';
            this.driverState.setAvailable(true);
            return;
          }

          this.driverState.setAvailable(false);
          const rideId = this.resolvePreferredRideId(this.acceptedRides);
          this.selectRide(rideId);
        },
        error: () => {
          this.error = 'Unable to load assigned rides.';
        },
      });
  }

  private resolvePreferredRideId(rides: DriverRideDetails[]): number {
    const selectedRideId = this.ride?.rideId;
    if (selectedRideId && rides.some((r) => r.rideId === selectedRideId)) return selectedRideId;

    const preferredRideId = this.preferredRideId;
    if (preferredRideId && rides.some((r) => r.rideId === preferredRideId)) {
      this.preferredRideId = null;
      return preferredRideId;
    }

    return rides[0].rideId;
  }

  selectRide(rideId: number): void {
    const nextRide = this.acceptedRides.find((r) => r.rideId === rideId);
    if (!nextRide) return;

    const changed = this.ride?.rideId !== nextRide.rideId;
    this.ride = nextRide;
    this.error = null;

    if (!changed && this.tracking) return;

    this.tracking = null;
    this.clearRouteFromMap();
    this.loadTrackingOnce(nextRide.rideId);
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
        ? t.route.map((p) => [p.lat, p.lng] as L.LatLngExpression)
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
      t.checkpoints.forEach((cp) => {
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

    this.markerLayers.forEach((layer) => {
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
    this.loadAcceptedRides();
  }

  private parseRideId(raw: string | null): number | null {
    const parsed = Number(raw);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
  }
}
