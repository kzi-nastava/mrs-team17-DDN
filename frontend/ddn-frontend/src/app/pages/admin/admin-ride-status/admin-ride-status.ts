import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

import {
  AdminRideStatusHttpDataSource,
  AdminRideStatusRowDto
} from '../../../api/admin/admin-ride-status.http.datasource';

import { API_BASE_URL } from '../../../app.config';

import * as L from 'leaflet';

type LatLngDto = { lat: number; lng: number };
type RideCheckpointDto = { order: number; address: string; lat: number; lng: number };

type RideTrackingResponseDto = {
  status?: string;
  pickup?: LatLngDto;
  destination?: LatLngDto;
  checkpoints?: RideCheckpointDto[];
  etaMinutes?: number;
  distanceKm?: number;
  route?: LatLngDto[];
};

@Component({
  selector: 'app-admin-ride-status',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-ride-status.html',
  styleUrl: './admin-ride-status.css',
})
export class AdminRideStatus implements OnInit {
  private api = inject(AdminRideStatusHttpDataSource);
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  query = '';
  loading = true;
  rows: AdminRideStatusRowDto[] = [];
  error = '';
  lastUpdatedAt: Date | null = null;

  expandedRideId: number | null = null;
  detailsLoading = false;
  detailsError = '';
  details: RideTrackingResponseDto | null = null;

  private miniMap: L.Map | null = null;
  private carMarker: L.CircleMarker | null = null;
  private miniPolyline: L.Polyline | null = null;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.error = '';
    this.collapse();

    this.api.list(this.query, 50).subscribe({
      next: (res) => {
        this.rows = (res ?? []).slice().sort((a, b) => this.startedAtMs(b.startedAt) - this.startedAtMs(a.startedAt));
        this.lastUpdatedAt = new Date();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load ride status';
        this.loading = false;
      },
    });
  }

  clearQuery(): void {
    if (!this.query.trim()) return;
    this.query = '';
    this.reload();
  }

  toggleDetails(r: AdminRideStatusRowDto): void {
    if (this.expandedRideId === r.rideId) {
      this.collapse();
      return;
    }

    this.expandedRideId = r.rideId;
    this.details = null;
    this.detailsError = '';
    this.detailsLoading = true;

    this.destroyMiniMap();

    this.http
      .get<RideTrackingResponseDto>(`${this.baseUrl}/rides/${r.rideId}/tracking`)
      .subscribe({
        next: (res) => {
          this.details = res ?? null;
          this.detailsLoading = false;

          setTimeout(() => {
            const pos = { lat: r.carLat, lng: r.carLng };
            this.renderMiniMap(
              r.rideId,
              pos,
              this.details?.route ?? [],
              this.details?.pickup,
              this.details?.destination,
              this.details?.checkpoints ?? []
            );
          }, 0);
        },
        error: () => {
          this.detailsError = 'Failed to load ride details';
          this.detailsLoading = false;

          setTimeout(() => {
            this.renderMiniMap(
              r.rideId,
              { lat: r.carLat, lng: r.carLng },
              []
            );
          }, 0);
        },
      });
  }

  private collapse(): void {
    this.expandedRideId = null;
    this.details = null;
    this.detailsError = '';
    this.detailsLoading = false;
    this.destroyMiniMap();
  }

  private renderMiniMap(
    rideId: number,
    car: LatLngDto,
    route: LatLngDto[],
    pickup?: LatLngDto,
    destination?: LatLngDto,
    checkpoints: RideCheckpointDto[] = []
  ): void {
    const el = document.getElementById(`mini-map-${rideId}`);
    if (!el) return;

    el.innerHTML = '';

    this.miniMap = L.map(el, {
      zoomControl: false,
      attributionControl: false,
      dragging: true,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      boxZoom: false,
      keyboard: false,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
    }).addTo(this.miniMap);

    // 🚗 auto kao mala tačkica
    this.carMarker = L.circleMarker([car.lat, car.lng], {
      radius: 6,
      color: '#2ecc71',
      fillColor: '#2ecc71',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.miniMap);

    // ruta ako postoji
    if (route && route.length >= 2) {
      const latlngs = route.map(p => [p.lat, p.lng] as [number, number]);
      this.miniPolyline = L.polyline(latlngs, {
      weight: 3,
      color: '#3498db',
      }).addTo(this.miniMap);
    }

    if (this.isValidPoint(pickup)) {
      L.circleMarker([pickup.lat, pickup.lng], {
        radius: 5,
        color: '#5dade2',
        fillColor: '#5dade2',
        fillOpacity: 0.95,
        weight: 2,
      }).addTo(this.miniMap);
    }

    if (this.isValidPoint(destination)) {
      L.circleMarker([destination.lat, destination.lng], {
        radius: 5,
        color: '#e74c3c',
        fillColor: '#e74c3c',
        fillOpacity: 0.95,
        weight: 2,
      }).addTo(this.miniMap);
    }

    for (const cp of checkpoints ?? []) {
      if (!this.isValidPoint(cp)) continue;
      L.circleMarker([cp.lat, cp.lng], {
        radius: 4,
        color: '#f1c40f',
        fillColor: '#f1c40f',
        fillOpacity: 0.9,
        weight: 2,
      })
        .addTo(this.miniMap)
        .bindTooltip(`#${cp.order}`, { permanent: false });
    }

    const bounds = L.latLngBounds([[car.lat, car.lng]]);

    if (this.miniPolyline) {
      bounds.extend(this.miniPolyline.getBounds());
    }
    if (this.isValidPoint(pickup)) bounds.extend([pickup.lat, pickup.lng]);
    if (this.isValidPoint(destination)) bounds.extend([destination.lat, destination.lng]);
    for (const cp of checkpoints ?? []) {
      if (!this.isValidPoint(cp)) continue;
      bounds.extend([cp.lat, cp.lng]);
    }

    if (bounds.isValid()) {
      this.miniMap.fitBounds(bounds, { padding: [14, 14] });
      return;
    }
    this.miniMap.setView([car.lat, car.lng], 15);
  }

  onRowKeydown(event: KeyboardEvent, r: AdminRideStatusRowDto): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggleDetails(r);
    }
  }

  statusLabel(status: string | null | undefined): string {
    const normalized = this.normalizeStatus(status);
    switch (normalized) {
      case 'ACTIVE':
        return 'Active';
      case 'COMPLETED':
        return 'Completed';
      case 'CANCELED':
        return 'Canceled';
      default:
        return normalized;
    }
  }

  statusClass(status: string | null | undefined): string {
    const normalized = this.normalizeStatus(status);
    switch (normalized) {
      case 'ACTIVE':
        return 'status-active';
      case 'COMPLETED':
        return 'status-completed';
      case 'CANCELED':
        return 'status-canceled';
      default:
        return 'status-unknown';
    }
  }

  formatStartedAt(startedAt: string | null): string {
    if (!startedAt) return 'Start time unavailable';
    const dt = new Date(startedAt);
    if (Number.isNaN(dt.getTime())) return 'Start time unavailable';
    return dt.toLocaleString();
  }

  etaLabel(etaMinutes: number | null | undefined): string {
    if (etaMinutes == null || !Number.isFinite(etaMinutes)) return '—';
    if (etaMinutes < 1) return '< 1 min';
    return `${Math.round(etaMinutes)} min`;
  }

  distanceLabel(distanceKm: number | null | undefined): string {
    if (distanceKm == null || !Number.isFinite(distanceKm)) return '—';
    return `${distanceKm.toFixed(distanceKm < 10 ? 1 : 0)} km`;
  }

  routePointCount(route: LatLngDto[] | null | undefined): string {
    const count = route?.length ?? 0;
    return count > 0 ? String(count) : '—';
  }

  private destroyMiniMap(): void {
    try {
      if (this.miniMap) {
        this.miniMap.remove();
      }
    } catch {
      // ignore
    }
    this.miniMap = null;
    this.carMarker = null;
    this.miniPolyline = null;
  }

  private normalizeStatus(status: string | null | undefined): string {
    const normalized = (status ?? '').trim().toUpperCase();
    return normalized || 'UNKNOWN';
  }

  private startedAtMs(startedAt: string | null): number {
    if (!startedAt) return 0;
    const ts = new Date(startedAt).getTime();
    return Number.isNaN(ts) ? 0 : ts;
  }

  private isValidPoint(point: LatLngDto | RideCheckpointDto | null | undefined): point is LatLngDto {
    return !!point && Number.isFinite(point.lat) && Number.isFinite(point.lng);
  }
}
