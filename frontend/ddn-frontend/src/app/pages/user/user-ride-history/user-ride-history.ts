import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';
import * as L from 'leaflet';

import { AuthStore } from '../../../api/auth/auth.store';
import { PassengerRidesHttpDataSource } from '../../../api/user/passenger-rides.http.datasource';
import { PassengerRideHistoryItem } from '../../../api/user/models/passenger-rides.models';
import { FavoriteRoutesApiService } from '../../../api/user/favorite-routes.http-data-source';
import { ESortBy } from '../../../api/user/models/enums/ESortBy';
import { ESortDirection } from '../../../api/user/models/enums/ESortDirection';

import { RidePassengerDetailsHttpDataSource } from '../../../api/user/ride-passenger-details.http-data-source';
import { RidePassengerDetails } from '../../../api/user/models/ride-passenger-details.models';
import { RideOrderApiService, CreateRideRequestDto } from '../../../api/user/ride-order.http-data-source';

// Leaflet's default marker icons reference relative asset paths that break
// under Angular's build pipeline. Point them at the CDN instead.
const DEFAULT_ICON = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});
L.Marker.prototype.options.icon = DEFAULT_ICON;

@Component({
  selector: 'app-user-ride-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-ride-history.html',
  styleUrl: './user-ride-history.css',
})
export class UserRideHistory implements OnInit {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);
  private readonly ratingWindowMs = 3 * 24 * 60 * 60 * 1000;
  public readonly ESortBy = ESortBy;
  public sortDirection = ESortDirection.DESC;
  public sortBy = ESortBy.STARTED_AT;

  userId!: number;

  rides: PassengerRideHistoryItem[] = [];
  isLoading = false;
  errorMsg = '';

  fromDate: string | null = null;
  toDate: string | null = null;

  addingRideId: number | null = null;

  readonly addedRideIds = new Set<number>();

  // --- Info popup state ---
  @ViewChild('rideMap') rideMapEl?: ElementRef<HTMLDivElement>;

  selectedRide: RidePassengerDetails | null = null;
  detailsLoading = false;
  detailsError = '';

  private map: L.Map | null = null;

  repeatMode: 'idle' | 'choosing-time' | 'submitting' = 'idle';
  scheduledAtLocal = ''; // bound to <input type="datetime-local">
  repeatError = '';
  repeatSuccessMsg = '';

  constructor(
    private ridesApi: PassengerRidesHttpDataSource,
    private favApi: FavoriteRoutesApiService,
    private rideDetailsApi: RidePassengerDetailsHttpDataSource,
    private rideOrderApi: RideOrderApiService
  ) {}

  ngOnInit(): void {
    const token = this.authStore.getToken();
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    const role = this.authStore.getRoleFromToken(token);
    if (role !== 'PASSENGER') {
      this.router.navigate(['/login']);
      return;
    }

    const id = this.authStore.getUserIdFromToken(token);
    if (!id) {
      this.router.navigate(['/login']);
      return;
    }

    this.userId = id;
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.ridesApi
      .getMyRideHistory(this.fromDate, this.toDate, this.sortBy, this.sortDirection)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: data => {
          this.rides = data ?? [];
        },
        error: (err: HttpErrorResponse) => {
          this.rides = [];
          this.errorMsg = this.extractMsg(err, 'Failed to load ride history.');
        },
      });
  }

  addToFavourites(rideId: number): void {
    if (this.addedRideIds.has(rideId)) return;
    if (this.addingRideId !== null) return;

    this.errorMsg = '';
    this.addingRideId = rideId;

    this.favApi
      .addFromRide(this.userId, rideId)
      .pipe(finalize(() => (this.addingRideId = null)))
      .subscribe({
        next: () => {
          this.addedRideIds.add(rideId);
        },
        error: (err: HttpErrorResponse) => {
          this.errorMsg = this.extractMsg(err, 'Failed to add route to favourites.');
        },
      });
  }

  openRate(ride: PassengerRideHistoryItem): void {
    if (this.isRatingExpired(ride)) {
      this.errorMsg = 'Rating is available up to 3 days after ride completion.';
      return;
    }

    this.router.navigate(['/user/rides', ride.rideId, 'rate'], {
      queryParams: { startedAt: ride.startedAt },
    });
  }

  isRatingExpired(ride: PassengerRideHistoryItem): boolean {
    const startedAtMs = this.toTimestamp(ride.startedAt);
    if (startedAtMs == null) return false;

    return Date.now() > startedAtMs + this.ratingWindowMs;
  }

  rateButtonLabel(ride: PassengerRideHistoryItem): string {
    return this.isRatingExpired(ride) ? 'Expired' : 'Rate';
  }

  rateButtonTitle(ride: PassengerRideHistoryItem): string {
    if (!this.isRatingExpired(ride)) return 'Rate this ride';
    return 'Rating is available up to 3 days after ride completion';
  }

  formatDate(value: string | null | undefined): string {
    if (!value) return '—';

    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return value;

    return d.toLocaleString(undefined, {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  cleanStops(stops: string[] | null | undefined): string[] {
    return (stops || [])
      .map(s => (s || '').trim())
      .filter(Boolean);
  }

  // ---------------------------------------------------------------------
  // Info popup: map + driver + reports + rating + repeat ride
  // ---------------------------------------------------------------------

  openRideInfo(ride: PassengerRideHistoryItem): void {
    this.selectedRide = null;
    this.detailsError = '';
    this.repeatMode = 'idle';
    this.repeatError = '';
    this.repeatSuccessMsg = '';
    this.detailsLoading = true;

    this.rideDetailsApi
      .getRideDetails(ride.rideId)
      .pipe(finalize(() => (this.detailsLoading = false)))
      .subscribe({
        next: details => {
          this.selectedRide = details;
          // wait a tick so the modal (and #rideMap div) is actually in the DOM
          setTimeout(() => this.initMap(details), 0);
        },
        error: (err: HttpErrorResponse) => {
          this.detailsError = this.extractMsg(err, 'Failed to load ride details.');
        },
      });
  }

  closeRideInfo(): void {
    this.selectedRide = null;
    this.detailsError = '';
    this.repeatMode = 'idle';
    this.repeatError = '';
    this.repeatSuccessMsg = '';
    this.destroyMap();
  }

  private initMap(details: RidePassengerDetails): void {
    this.destroyMap();
    if (!this.rideMapEl) return;

    const map = L.map(this.rideMapEl.nativeElement);
    this.map = map;

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(map);

    const bounds: L.LatLngExpression[] = [];

    if (details.route && details.route.length > 1) {
      const latlngs = details.route.map(p => [p.lat, p.lng] as L.LatLngExpression);
      L.polyline(latlngs, { color: '#4a4ee8', weight: 4 }).addTo(map);
      bounds.push(...latlngs);
    }

    if (details.start) {
      L.marker([details.start.lat, details.start.lng]).addTo(map).bindPopup('Start: ' + details.startAddress);
      bounds.push([details.start.lat, details.start.lng]);
    }

    (details.stops || []).forEach(stop => {
      L.marker([stop.lat, stop.lng]).addTo(map).bindPopup('Stop: ' + stop.address);
      bounds.push([stop.lat, stop.lng]);
    });

    if (details.destination) {
      L.marker([details.destination.lat, details.destination.lng])
        .addTo(map)
        .bindPopup('Destination: ' + details.destinationAddress);
      bounds.push([details.destination.lat, details.destination.lng]);
    }

    if (bounds.length > 0) {
      map.fitBounds(L.latLngBounds(bounds), { padding: [24, 24] });
    } else {
      map.setView([44.7866, 20.4489], 12); // fallback view
    }

    // the map is created while the modal's CSS transition/layout settles;
    // recalculate size once more to avoid a half-rendered tile grid
    setTimeout(() => map.invalidateSize(), 100);
  }

  private destroyMap(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  startRepeatNow(): void {
    if (!this.selectedRide) return;
    this.repeatError = '';
    this.repeatSuccessMsg = '';
    this.repeatMode = 'submitting';

    const request = this.buildCreateRideRequest(this.selectedRide, 'now', null);

    this.rideOrderApi
      .createRide(request)
      .pipe(finalize(() => (this.repeatMode = 'idle')))
      .subscribe({
        next: () => {
          this.repeatSuccessMsg = 'Ride ordered! Check your active ride.';
        },
        error: (err: HttpErrorResponse) => {
          this.repeatError = this.extractMsg(err, 'Failed to order this ride.');
        },
      });
  }

  startRepeatLater(): void {
    this.repeatError = '';
    this.repeatSuccessMsg = '';
    this.scheduledAtLocal = '';
    this.repeatMode = 'choosing-time';
  }

  cancelRepeatLater(): void {
    this.repeatMode = 'idle';
  }

  confirmRepeatLater(): void {
    if (!this.selectedRide) return;

    if (!this.scheduledAtLocal) {
      this.repeatError = 'Pick a date and time first.';
      return;
    }

    const scheduledAtIso = new Date(this.scheduledAtLocal).toISOString();
    this.repeatError = '';
    this.repeatMode = 'submitting';

    const request = this.buildCreateRideRequest(this.selectedRide, 'schedule', scheduledAtIso);

    this.rideOrderApi
      .createRide(request)
      .pipe(finalize(() => (this.repeatMode = 'idle')))
      .subscribe({
        next: () => {
          this.repeatSuccessMsg = 'Ride scheduled!';
        },
        error: (err: HttpErrorResponse) => {
          this.repeatError = this.extractMsg(err, 'Failed to schedule this ride.');
        },
      });
  }

  private buildCreateRideRequest(
    details: RidePassengerDetails,
    orderType: 'now' | 'schedule',
    scheduledAtIso: string | null
  ): CreateRideRequestDto {
    return {
      requesterUserId: this.userId,
      orderType,
      scheduledAt: scheduledAtIso,
      start: {
        address: details.startAddress,
        lat: details.start?.lat ?? 0,
        lng: details.start?.lng ?? 0,
      },
      destination: {
        address: details.destinationAddress,
        lat: details.destination?.lat ?? 0,
        lng: details.destination?.lng ?? 0,
      },
      checkpoints: (details.stops || []).map(s => ({
        address: s.address,
        lat: s.lat,
        lng: s.lng,
      })),
      vehicleType: details.vehicleType as 'standard' | 'luxury' | 'van',
      babyTransport: details.babyTransport,
      petTransport: details.petTransport,
    };
  }

  driverDisplayName(details: RidePassengerDetails | null): string {
    if (!details?.driver) return 'Not assigned';
    return `${details.driver.firstName ?? ''} ${details.driver.lastName ?? ''}`.trim() || 'Not assigned';
  }

  // ---------------------------------------------------------------------

  private extractMsg(err: HttpErrorResponse, fallback: string): string {
    return (
      (err as any)?.error?.message ||
      (typeof (err as any)?.error === 'string' ? (err as any).error : '') ||
      fallback
    );
  }

  private toTimestamp(value: string | null | undefined): number | null {
    if (!value) return null;
    const ts = new Date(value).getTime();
    return Number.isFinite(ts) ? ts : null;
  }

  doSort(sortBy: ESortBy) {
    if (this.sortBy == sortBy) {
      this.sortDirection = this.sortDirection == ESortDirection.ASC ? ESortDirection.DESC : ESortDirection.ASC;
    } else {
      this.sortDirection = ESortDirection.DESC;
    }
    this.sortBy = sortBy;

    this.load();
  }
}
