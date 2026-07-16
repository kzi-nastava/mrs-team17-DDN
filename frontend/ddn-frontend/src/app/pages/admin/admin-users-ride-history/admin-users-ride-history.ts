import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';

import { API_BASE_URL } from '../../../app.config';

import * as L from 'leaflet';

/* ===== Shapes returned by the backend (org.example.backend.dto.response.*) ===== */

interface AdminUserOptionDto {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
}

interface AdminRideHistoryItemDto {
  rideId: number;
  startAddress: string;
  destinationAddress: string;
  route: string;
  startDate: string | null;
  endDate: string | null;
  status: string;
  canceled: boolean;
  canceledBy: string | null;
  price: number;
  panicActivated: boolean;
}

type LatLngDto = { lat: number; lng: number };
type RideCheckpointDto = { stopOrder: number; address: string; lat: number; lng: number };

interface DriverPublicInfoDto {
  driverId: number;
  firstName: string;
  lastName: string;
  profileImageUrl: string | null;
  vehicleModel: string | null;
  vehicleType: string | null;
  licensePlate: string | null;
}

interface PassengerInfoDto {
  name: string;
  email: string;
}

interface RideReportDto {
  id: number;
  rideId: number;
  description: string;
  createdAt: string;
}

interface RideRatingDto {
  id: number;
  rideId: number;
  driverRating: number;
  vehicleRating: number;
  comment: string | null;
  createdAt: string;
}

interface AdminRideDetailsResponseDto {
  rideId: number;
  startAddress: string;
  destinationAddress: string;
  start: LatLngDto | null;
  destination: LatLngDto | null;
  stops: RideCheckpointDto[];
  route: LatLngDto[];
  distanceKm: number;
  startDate: string | null;
  endDate: string | null;
  status: string;
  canceled: boolean;
  canceledBy: string | null;
  cancelReason: string | null;
  price: number;
  panicActivated: boolean;
  driver: DriverPublicInfoDto | null;
  passengers: PassengerInfoDto[];
  reports: RideReportDto[];
  rating: RideRatingDto | null;
  vehicleType: string;
  babyTransport: boolean;
  petTransport: boolean;
}

type SortField =
  | 'startDate'
  | 'endDate'
  | 'route'
  | 'status'
  | 'canceled'
  | 'canceledBy'
  | 'price'
  | 'panicActivated';

@Component({
  selector: 'app-admin-users-ride-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users-ride-history.html',
  styleUrl: './admin-users-ride-history.css',
})
export class AdminUsersRideHistory implements OnInit, OnDestroy {
  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  passengers: AdminUserOptionDto[] = [];
  selectedPassenger: number | '' = '';

  fromDate = '';
  toDate = '';

  sortField: SortField = 'startDate';
  sortDir: 'asc' | 'desc' = 'desc';

  rides: AdminRideHistoryItemDto[] = [];
  isLoading = false;
  errorMsg = '';

  // expandable detail row
  expandedRideId: number | null = null;
  details: AdminRideDetailsResponseDto | null = null;
  detailsLoading = false;
  detailsError = '';

  // reorder ("order again")
  reorderScheduledAt = '';
  reorderBusy = false;
  reorderMsg = '';

  private miniMap: L.Map | null = null;

  ngOnInit(): void {
    this.loadPassengers();
    this.load();
  }

  ngOnDestroy(): void {
    this.destroyMiniMap();
  }

  loadPassengers(): void {
    this.http
      .get<AdminUserOptionDto[]>(`${this.baseUrl}/admin/users`, {
        params: new HttpParams().set('role', 'PASSENGER').set('limit', 500),
      })
      .subscribe({
        next: (res) => (this.passengers = res ?? []),
        error: () => (this.passengers = []),
      });
  }

  passengerName(d: AdminUserOptionDto): string {
    return `${d.firstName ?? ''} ${d.lastName ?? ''}`.trim() || d.email;
  }

  load(): void {
    this.isLoading = true;
    this.errorMsg = '';
    this.collapse();

    let params = new HttpParams().set('sort', this.sortField).set('dir', this.sortDir);
    if (this.fromDate) params = params.set('from', this.fromDate);
    if (this.toDate) params = params.set('to', this.toDate);

    const url = this.selectedPassenger
      ? `${this.baseUrl}/admin/users/${this.selectedPassenger}/rides`
      : `${this.baseUrl}/admin/rides`;

    this.http.get<AdminRideHistoryItemDto[]>(url, { params }).subscribe({
      next: (res) => {
        this.rides = res ?? [];
        this.isLoading = false;
      },
      error: () => {
        this.errorMsg = 'Failed to load ride history';
        this.isLoading = false;
      },
    });
  }

  sortBy(field: SortField): void {
    if (this.sortField === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDir = field === 'route' || field === 'canceledBy' ? 'asc' : 'desc';
    }
    this.load();
  }

  sortIndicator(field: SortField): string {
    if (this.sortField !== field) return '';
    return this.sortDir === 'asc' ? '▲' : '▼';
  }

  toggleDetails(r: AdminRideHistoryItemDto): void {
    if (this.expandedRideId === r.rideId) {
      this.collapse();
      return;
    }

    this.expandedRideId = r.rideId;
    this.details = null;
    this.detailsError = '';
    this.detailsLoading = true;
    this.reorderMsg = '';
    this.reorderScheduledAt = '';
    this.destroyMiniMap();

    this.http.get<AdminRideDetailsResponseDto>(`${this.baseUrl}/admin/rides/${r.rideId}`).subscribe({
      next: (res) => {
        this.details = res;
        this.detailsLoading = false;
        setTimeout(() => this.renderMiniMap(r.rideId), 0);
      },
      error: () => {
        this.detailsError = 'Failed to load ride details';
        this.detailsLoading = false;
      },
    });
  }

  reorderNow(rideId: number): void {
    this.doReorder(rideId, null);
  }

  reorderLater(rideId: number): void {
    if (!this.reorderScheduledAt) {
      this.reorderMsg = 'Pick a date and time first.';
      return;
    }
    this.doReorder(rideId, new Date(this.reorderScheduledAt).toISOString());
  }

  private doReorder(rideId: number, scheduledAt: string | null): void {
    this.reorderBusy = true;
    this.reorderMsg = '';

    this.http
      .post<{ rideId: number }>(`${this.baseUrl}/admin/rides/${rideId}/reorder`, {
        scheduledAt,
      })
      .subscribe({
        next: (res) => {
          this.reorderBusy = false;
          this.reorderMsg = scheduledAt
            ? `Scheduled new ride #${res.rideId} for the same route.`
            : `Ordered new ride #${res.rideId} for the same route.`;
        },
        error: (err) => {
          this.reorderBusy = false;
          this.reorderMsg = err?.error?.message || 'Could not place that order.';
        },
      });
  }

  private collapse(): void {
    this.expandedRideId = null;
    this.details = null;
    this.detailsError = '';
    this.detailsLoading = false;
    this.reorderMsg = '';
    this.destroyMiniMap();
  }

  private renderMiniMap(rideId: number): void {
    const el = document.getElementById(`admin-user-ride-map-${rideId}`);
    if (!el || !this.details) return;

    this.miniMap = L.map(el, {
      zoomControl: false,
      attributionControl: false,
      scrollWheelZoom: false,
      doubleClickZoom: false,
      boxZoom: false,
      keyboard: false,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(this.miniMap);

    const bounds = L.latLngBounds([]);

    if (this.details.route?.length >= 2) {
      const latlngs = this.details.route.map((p) => [p.lat, p.lng] as [number, number]);
      L.polyline(latlngs, { weight: 3, color: '#3498db' }).addTo(this.miniMap);
      latlngs.forEach((p) => bounds.extend(p));
    }

    if (this.isValidPoint(this.details.start)) {
      L.circleMarker([this.details.start!.lat, this.details.start!.lng], {
        radius: 6,
        color: '#5dade2',
        fillColor: '#5dade2',
        fillOpacity: 0.95,
        weight: 2,
      }).addTo(this.miniMap);
      bounds.extend([this.details.start!.lat, this.details.start!.lng]);
    }

    if (this.isValidPoint(this.details.destination)) {
      L.circleMarker([this.details.destination!.lat, this.details.destination!.lng], {
        radius: 6,
        color: '#e74c3c',
        fillColor: '#e74c3c',
        fillOpacity: 0.95,
        weight: 2,
      }).addTo(this.miniMap);
      bounds.extend([this.details.destination!.lat, this.details.destination!.lng]);
    }

    for (const cp of this.details.stops ?? []) {
      if (!this.isValidPoint(cp)) continue;
      L.circleMarker([cp.lat, cp.lng], {
        radius: 4,
        color: '#f1c40f',
        fillColor: '#f1c40f',
        fillOpacity: 0.9,
        weight: 2,
      })
        .addTo(this.miniMap)
        .bindTooltip(`#${cp.stopOrder}`, { permanent: false });
      bounds.extend([cp.lat, cp.lng]);
    }

    if (bounds.isValid()) {
      this.miniMap.fitBounds(bounds, { padding: [14, 14] });
    } else {
      this.miniMap.setView([0, 0], 2);
    }
  }

  private destroyMiniMap(): void {
    try {
      this.miniMap?.remove();
    } catch {
      // ignore
    }
    this.miniMap = null;
  }

  private isValidPoint(point: LatLngDto | RideCheckpointDto | null | undefined): point is LatLngDto {
    return !!point && Number.isFinite(point.lat) && Number.isFinite(point.lng);
  }

  formatDate(iso: string | null): string {
    if (!iso) return '—';
    const dt = new Date(iso);
    return Number.isNaN(dt.getTime()) ? '—' : dt.toLocaleString();
  }

  formatPrice(price: number | null | undefined): string {
    return price == null ? '—' : `${price.toFixed(2)} RSD`;
  }

  statusClass(status: string | null | undefined): string {
    switch ((status ?? '').toUpperCase()) {
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
}
