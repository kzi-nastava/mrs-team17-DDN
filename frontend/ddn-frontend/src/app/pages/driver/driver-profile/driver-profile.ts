import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { DriverProfileHttpDataSource } from '../../../api/driver/driver-profile.http-data-source';
import {
  DriverProfileResponseDto,
  UpdateDriverProfileRequestDto,
} from '../../../api/driver/models/driver-profile.models';
import { API_BASE_URL } from '../../../app.config';
import { DriverStateService } from '../../../state/driver-state.service';
import { AuthStore } from '../../../api/auth/auth.store';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './driver-profile.html',
  styleUrl: './driver-profile.css',
})
export class DriverProfile implements OnInit {
  private readonly baseUrl = inject(API_BASE_URL);
  private readonly backendOrigin = this.baseUrl.replace(/\/api\/?$/, '');

  private readonly driverState = inject(DriverStateService);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  driverId!: number;

  profile: DriverProfileResponseDto | null = null;

  loading = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;

  form: UpdateDriverProfileRequestDto = {
    firstName: '',
    lastName: '',
    address: '',
    phoneNumber: '',
    profileImageUrl: '',
  };

  maxActiveMinutes = 8 * 60;
  activeFillPct = 0;
  activeLabel = '0h 0min';

  private imageBust = 0;

  constructor(private api: DriverProfileHttpDataSource) {}

  ngOnInit(): void {
    const id = this.driverState.getDriverIdSnapshot() ?? this.authStore.getCurrentDriverId();

    if (!id) {
      this.router.navigate(['/login']);
      return;
    }

    this.driverId = id;
    this.driverState.setDriverId(id);

    this.loadProfile();
  }

  get avatarUrl(): string {
    const raw =
      this.form.profileImageUrl ||
      this.profile?.driver?.profileImageUrl ||
      '';

    const resolved = this.resolveImageUrl(raw);

    if (!resolved) return 'avatar.svg';

    if (this.imageBust && resolved.includes('/public/profile-images/')) {
      const join = resolved.includes('?') ? '&' : '?';
      return `${resolved}${join}v=${this.imageBust}`;
    }

    return resolved;
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMsg = null;
    this.successMsg = null;

    this.api.getProfile(this.driverId).subscribe({
      next: (res) => {
        this.profile = res;

        this.form.firstName = res.driver.firstName ?? '';
        this.form.lastName = res.driver.lastName ?? '';
        this.form.address = res.driver.address ?? '';
        this.form.phoneNumber = res.driver.phoneNumber ?? '';

        this.form.profileImageUrl = res.driver.profileImageUrl ?? '';

        this.setActiveTime(res.activeMinutesLast24h ?? 0);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Cannot load profile data.';
      },
    });
  }

  onUpdateProfile(): void {
    this.loading = true;
    this.errorMsg = null;
    this.successMsg = null;

    const payload: UpdateDriverProfileRequestDto = {
      firstName: this.form.firstName?.trim(),
      lastName: this.form.lastName?.trim(),
      address: this.form.address?.trim(),
      phoneNumber: this.form.phoneNumber?.trim(),

      profileImageUrl: this.normalizeStoredUrl(this.form.profileImageUrl),
    };

    this.api.requestProfileChange(this.driverId, payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMsg = `Request sent to admins for review (status: ${res.status}).`;
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Cannot send request.';
      },
    });
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    this.loading = true;
    this.errorMsg = null;
    this.successMsg = null;

    this.api.uploadProfileImage(this.driverId, file).subscribe({
      next: (res) => {
        this.form.profileImageUrl = res.profileImageUrl || '';

        this.imageBust = Date.now();

        this.loading = false;
        this.successMsg = 'Image uploaded. Don’t forget to send profile update request.';
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Image upload failed.';
      },
    });

    input.value = '';
  }

  private resolveImageUrl(url: string | null | undefined): string {
    const u = (url ?? '').trim();
    if (!u) return '';

    if (/^https?:\/\//i.test(u)) return u;

    if (u.startsWith('/public/')) return `${this.backendOrigin}${u}`;

    return u;
  }

  private normalizeStoredUrl(url: string | null | undefined): string {
    const u = (url ?? '').trim();
    if (!u) return '';

    if (/^https?:\/\//i.test(u) && u.startsWith(this.backendOrigin)) {
      return u.substring(this.backendOrigin.length);
    }

    return u;
  }

  private setActiveTime(activeMinutes: number): void {
    const safe = Math.max(0, activeMinutes);
    const h = Math.floor(safe / 60);
    const m = safe % 60;

    this.activeLabel = `${h}h ${m}min`;
    if (h === 0 && m === 0) this.activeLabel = '';

    const pct = (safe / this.maxActiveMinutes) * 100;
    this.activeFillPct = Math.max(0, Math.min(100, Math.round(pct)));
  }
}
