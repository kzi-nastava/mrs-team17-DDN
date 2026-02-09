import { AfterViewInit, Component, OnDestroy, inject } from '@angular/core';
import * as L from 'leaflet';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { UserNavbarComponent } from '../../../components/user-navbar/user-navbar';
import { RIDE_TRACKING_DS } from '../../../api/user/ride-tracking.datasource';
import { RideCheckpoint, TrackingState } from '../../../api/user/models/ride-tracking.models';

type LatLng = { lat: number; lng: number };

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
  private checkpointMarkers: L.CircleMarker[] = [];

  private lastCar: LatLng | null = null;
  private carAnimFrame: number | null = null;

  etaMinutes = 0;
  distanceKm = 0;
  rideStatus = '';

  reportOpen = false;
  reportText = '';

  private initializedFromState = false;
  private trackingRideId?: number;

  ngAfterViewInit(): void {
    const rawRideId = this.route.snapshot.queryParamMap.get('rideId');
    const parsedRideId = Number(rawRideId);
    this.trackingRideId =
      Number.isFinite(parsedRideId) && parsedRideId > 0 ? parsedRideId : undefined;

    this.initMap();

    this.sub = this.ds.watchMyActiveTracking(this.trackingRideId).subscribe({
      next: (s) => this.applyState(s),
      error: () => (this.rideStatus = 'Tracking not available'),
    });

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.sub = null;

    if (this.carAnimFrame != null) {
      cancelAnimationFrame(this.carAnimFrame);
      this.carAnimFrame = null;
    }

    if (this.map) this.map.remove();
  }

  openReport(): void {
    if (this.rideStatus !== 'ACTIVE') return;
    this.reportOpen = true;
  }

  closeReport(): void {
    this.reportOpen = false;
    this.reportText = '';
  }

  submitReport(): void {
    if (this.rideStatus !== 'ACTIVE') return;

    const text = this.reportText.trim();
    if (text.length < 5) return;

    this.ds.submitInconsistencyForMyActiveRide(text, this.trackingRideId).subscribe({
      next: () => {
        this.reportOpen = false;
        this.reportText = '';
      },
      error: () => {
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

    const routePoints = (s.route ?? []).map((p) => [p.lat, p.lng] as [number, number]);

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

      this.lastCar = { ...s.car };

      this.drawCheckpoints(s.checkpoints ?? []);

      // RUTA SE CRTА JEDNOM: pickup -> destination (nikad se više ne menja)
      if (routePoints.length >= 2) {
        this.routeLine = L.polyline(routePoints, { weight: 4 }).addTo(this.map);
      } else {
        this.routeLine = L.polyline(
          [
            [s.pickup.lat, s.pickup.lng],
            [s.destination.lat, s.destination.lng],
          ],
          { weight: 4 }
        ).addTo(this.map);
      }

      // fit bounds (uključi i auto da se sve vidi)
      const bounds = L.latLngBounds([
        [s.car.lat, s.car.lng],
        [s.pickup.lat, s.pickup.lng],
        [s.destination.lat, s.destination.lng],
      ]);
      (s.checkpoints ?? []).forEach((cp) => bounds.extend([cp.lat, cp.lng]));
      this.map.fitBounds(bounds, { padding: [30, 30] });

      return;
    }

    // posle init-a: NE DIRAMO routeLine uopšte
    this.animateCarTo({ lat: s.car.lat, lng: s.car.lng }, 900);
  }
  private drawCheckpoints(checkpoints: RideCheckpoint[]): void {
    this.checkpointMarkers.forEach((m) => {
      try { m.remove(); } catch {}
    });
    this.checkpointMarkers = [];

    if (!this.map || !checkpoints || checkpoints.length === 0) return;

    checkpoints.forEach((cp) => {
      const m = L.circleMarker([cp.lat, cp.lng], {
        radius: 6,
        color: '#ffffff',
        weight: 2,
        fillColor: '#ff9800',
        fillOpacity: 0.95,
      })
        .addTo(this.map)
        .bindPopup(`Stop ${cp.stopOrder}: ${cp.address}`);

      this.checkpointMarkers.push(m);
    });
  }

  private animateCarTo(target: LatLng, durationMs: number): void {
    if (!this.carMarker) return;

    const from = this.lastCar ?? target;

    if (this.carAnimFrame != null) {
      cancelAnimationFrame(this.carAnimFrame);
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
}
