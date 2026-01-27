import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
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

  private rideId!: number;

  existing: RideRatingResponse | null = null;

  driverRating = 5;
  vehicleRating = 5;
  comment = '';

  submitting = false;
  info = '';
  error: string | null = null;

  ngOnInit(): void {
    const raw = this.route.snapshot.paramMap.get('rideId');
    this.rideId = Number(raw);

    if (!Number.isFinite(this.rideId) || this.rideId <= 0) {
      this.error = 'Invalid ride';
      return;
    }

    this.loadExisting();
  }

  private loadExisting(): void {
    this.info = 'Loading...';
    this.error = null;

    this.ds.getRating(this.rideId).subscribe({
      next: (r) => {
        this.existing = r;
        this.info = r ? 'You already rated this ride.' : '';
      },
      error: () => {
        this.info = '';
        this.error = 'Rating not available';
      },
    });
  }

  submit(): void {
    if (this.existing) return;

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
      error: () => {
        this.submitting = false;
        this.error = 'Submit failed';
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/user']);
  }
}
