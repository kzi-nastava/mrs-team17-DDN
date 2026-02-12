import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserNavbarComponent } from '../../../components/user-navbar/user-navbar';
import { RIDE_RATING_DS } from '../../../api/user/ride-rating.datasource';
import { RideRatingResponse } from '../../../api/user/models/ride-rating.models';

@Component({
  selector: 'app-ride-rate',
  standalone: true,
  imports: [CommonModule, FormsModule, UserNavbarComponent],
  templateUrl: './ride-rate.html',
  styleUrl: './ride-rate.css',
})
export class RideRateComponent implements OnInit {
  private ds = inject(RIDE_RATING_DS);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private readonly ratingWindowMs = 3 * 24 * 60 * 60 * 1000;

  private rideId!: number;
  private startedAt: string | null = null;

  existing: RideRatingResponse | null = null;

  driverRating = 5;
  vehicleRating = 5;
  comment = '';

  submitting = false;
  info = '';
  error: string | null = null;
  ratingWindowExpired = false;

  ngOnInit(): void {
    const raw = this.route.snapshot.paramMap.get('rideId');
    this.rideId = Number(raw);
    this.startedAt = this.route.snapshot.queryParamMap.get('startedAt');

    if (!Number.isFinite(this.rideId) || this.rideId <= 0) {
      this.error = 'Invalid ride';
      return;
    }

    this.ratingWindowExpired = this.isExpiredByStartedAt(this.startedAt);
    if (this.ratingWindowExpired) {
      this.info = 'Rating is available up to 3 days after ride completion.';
    }

    this.loadExisting();
  }

  private loadExisting(): void {
    this.info = 'Loading...';
    this.error = null;

    this.ds.getRating(this.rideId).subscribe({
      next: (r) => {
        this.existing = r;
        if (r) {
          this.info = 'You already rated this ride.';
        } else if (this.ratingWindowExpired) {
          this.info = 'Rating is available up to 3 days after ride completion.';
        } else {
          this.info = '';
        }
      },
      error: (err: HttpErrorResponse) => {
        this.info = '';
        this.error = this.extractErrorMessage(err, 'Rating not available');
      },
    });
  }

  submit(): void {
    if (this.existing) return;
    if (this.ratingWindowExpired) {
      this.error = 'Rating is available up to 3 days after ride completion.';
      return;
    }

    const dr = Number(this.driverRating);
    const vr = Number(this.vehicleRating);

    if (!Number.isFinite(dr) || dr < 1 || dr > 5) return;
    if (!Number.isFinite(vr) || vr < 1 || vr > 5) return;

    this.submitting = true;
    this.error = null;

    this.ds.submitRating(this.rideId, {
      driverRating: dr,
      vehicleRating: vr,
      comment: this.comment.trim() || undefined,
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.loadExisting();
      },
      error: (err: HttpErrorResponse) => {
        this.submitting = false;
        this.error = this.extractErrorMessage(err, 'Submit failed');
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/user']);
  }

  private isExpiredByStartedAt(value: string | null): boolean {
    if (!value) return false;
    const startedAtMs = new Date(value).getTime();
    if (!Number.isFinite(startedAtMs)) return false;
    return Date.now() > startedAtMs + this.ratingWindowMs;
  }

  private extractErrorMessage(err: HttpErrorResponse, fallback: string): string {
    const backendMsg =
      (err?.error as any)?.message ||
      (typeof err?.error === 'string' ? err.error : '');

    const msg = (backendMsg || '').toString().trim();
    if (!msg) return fallback;

    if (msg === 'Rating window expired') {
      return 'Rating is available up to 3 days after ride completion.';
    }
    if (msg === 'Rating already exists') {
      return 'You already rated this ride.';
    }
    if (msg === 'Ride is not completed') {
      return 'Ride must be completed before rating.';
    }

    return msg;
  }
}
