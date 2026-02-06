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
        this.rows = res ?? [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load ride status';
        this.loading = false;
      },
    });
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
            this.renderMiniMap(r.rideId, pos, this.details?.route ?? []);
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
    route: LatLngDto[]
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

      const bounds = this.miniPolyline.getBounds();
      bounds.extend([car.lat, car.lng]);
      this.miniMap.fitBounds(bounds, { padding: [12, 12] });
    } else {
      this.miniMap.setView([car.lat, car.lng], 15);
    }
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
}
