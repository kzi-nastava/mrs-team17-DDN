import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import * as L from 'leaflet';
import { EMPTY, Subscription, finalize, take, timer } from 'rxjs';
import { catchError, exhaustMap } from 'rxjs/operators';

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
  private carMarker: L.CircleMarker | null = null;
  private lastCar: LatLng | null = null;
  private carAnimFrame: number | null = null;
  private trackingSub: Subscription | null = null;

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
    this.trackingSub?.unsubscribe();
    this.trackingSub = null;
    if (this.carAnimFrame != null) {
      try { cancelAnimationFrame(this.carAnimFrame); } catch {}
      this.carAnimFrame = null;
    }
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
    this.trackingSub?.unsubscribe();
    this.trackingSub = null;

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

          this.startTracking(this.ride.rideId);
        },
        error: () => {
          this.error = 'Unable to load assigned rides.';
        },
      });
  }

  private startTracking(rideId: number): void {
    this.trackingSub?.unsubscribe();
    this.trackingSub = timer(0, 2000)
      .pipe(
        exhaustMap(() =>
          this.http.get<TrackingState>(`${this.baseUrl}/rides/${rideId}/tracking`, {
            params: { ts: Date.now().toString() },
          }).pipe(
            catchError(() => EMPTY)
          )
        )
      )
      .subscribe((t) => {
        if (!t) return;
        this.applyTrackingState(t);
      });
  }

  private applyTrackingState(t: TrackingState): void {
    this.tracking = t;
    if (!this.routeLine) {
      this.drawRouteOnMap(t);
    }
    this.upsertCarMarker(t.car);
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

    const bounds = this.routeLine.getBounds();
    bounds.extend([t.car.lat, t.car.lng]);
    this.map.fitBounds(bounds, { padding: [30, 30] });
    setTimeout(() => this.map.invalidateSize(), 0);
  }

  private upsertCarMarker(car: LatLng): void {
    if (!this.map) return;

    if (!this.carMarker) {
      this.carMarker = L.circleMarker([car.lat, car.lng], {
        radius: 7,
        color: '#2ecc71',
        fillColor: '#2ecc71',
        fillOpacity: 0.9,
        weight: 2,
      }).addTo(this.map);
      this.lastCar = { ...car };
      return;
    }

    this.animateCarTo(car, 900);
  }

  private animateCarTo(target: LatLng, durationMs: number): void {
    if (!this.carMarker) return;

    const from = this.lastCar ?? target;

    if (this.carAnimFrame != null) {
      try { cancelAnimationFrame(this.carAnimFrame); } catch {}
      this.carAnimFrame = null;
    }

    const start = performance.now();

    const step = (now: number) => {
      const t = Math.min(1, (now - start) / durationMs);

      const lat = from.lat + (target.lat - from.lat) * t;
      const lng = from.lng + (target.lng - from.lng) * t;

      this.carMarker!.setLatLng([lat, lng]);

      if (t < 1) {
        this.carAnimFrame = requestAnimationFrame(step);
      } else {
        this.carAnimFrame = null;
        this.lastCar = { ...target };
      }
    };

    this.carAnimFrame = requestAnimationFrame(step);
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

    if (this.carMarker) {
      try {
        this.map.removeLayer(this.carMarker);
      } catch {
      }
      this.carMarker = null;
    }
    if (this.carAnimFrame != null) {
      try { cancelAnimationFrame(this.carAnimFrame); } catch {}
      this.carAnimFrame = null;
    }
    this.lastCar = null;
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
        error: (err) => {
          this.error = this.extractErrorMessage(err, 'The drive start failed. Please try again.');
        },
      });
  }

  refresh(): void {
    if (this.loading || this.starting) return;
    this.loadAcceptedRide();
  }

  private extractErrorMessage(err: any, fallback: string): string {
    if (!err) return fallback;
    const body = err.error;
    if (typeof body === 'string' && body.trim()) return body;
    if (body?.message) return body.message;
    if (body?.detail) return body.detail;
    if (err?.message) return err.message;
    return fallback;
  }
}
