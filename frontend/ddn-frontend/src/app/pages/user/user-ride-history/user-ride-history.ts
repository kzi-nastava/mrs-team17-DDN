import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

import { AuthStore } from '../../../api/auth/auth.store';
import { PassengerRidesHttpDataSource } from '../../../api/user/passenger-rides.http.datasource';
import { PassengerRideHistoryItem } from '../../../api/user/models/passenger-rides.models';
import { FavoriteRoutesApiService } from '../../../api/user/favorite-routes.http-data-source';

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

  userId!: number;

  rides: PassengerRideHistoryItem[] = [];
  isLoading = false;
  errorMsg = '';

  fromDate: string | null = null;
  toDate: string | null = null;

  addingRideId: number | null = null;
  readonly addedRideIds = new Set<number>();

  constructor(
    private ridesApi: PassengerRidesHttpDataSource,
    private favApi: FavoriteRoutesApiService
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
      .getMyRideHistory(this.fromDate, this.toDate)
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
}
