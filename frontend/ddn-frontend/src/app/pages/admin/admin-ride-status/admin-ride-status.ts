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
  car?: LatLngDto;
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
  private miniMarker: L.Marker | null = null;
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

          // nacrtaj mapu nakon rendera DOM-a
          setTimeout(() => {
            const fallback = { lat: r.carLat, lng: r.carLng };
            const pos = this.details?.car ?? fallback;
            this.renderMiniMap(r.rideId, pos, this.details?.route ?? []);
          }, 0);
        },
        error: () => {
          this.detailsError = 'Failed to load ride details';
          this.detailsLoading = false;

          // i dalje nacrtaj mapu sa fallback koordinatama iz liste
          setTimeout(() => {
            this.renderMiniMap(r.rideId, { lat: r.carLat, lng: r.carLng }, []);
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

  private renderMiniMap(rideId: number, car: LatLngDto, route: LatLngDto[]): void {
    const elId = `mini-map-${rideId}`;
    const el = document.getElementById(elId);
    if (!el) return;

    // reset u slucaju da je ostalo nesto u containeru
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

    // tile layer (OSM)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
    }).addTo(this.miniMap);

    // marker (trenutna tacka)
    this.miniMarker = L.marker([car.lat, car.lng]).addTo(this.miniMap);

    // opcionalno: ako hoces i malu rutu (ako postoji)
    if (route && route.length >= 2) {
      const latlngs = route.map(p => [p.lat, p.lng] as [number, number]);
      this.miniPolyline = L.polyline(latlngs, { weight: 3 }).addTo(this.miniMap);

      const bounds = this.miniPolyline.getBounds();
      this.miniMap.fitBounds(bounds, { padding: [10, 10] });
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
    this.miniMarker = null;
    this.miniPolyline = null;
  }
}
