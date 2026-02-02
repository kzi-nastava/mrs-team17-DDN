import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { API_BASE_URL } from '../../../app.config';
import { AuthStore } from '../../../api/auth/auth.store';
import { UserProfileHttpDataSource } from '../../../api/user/user-profile.http-data-source';
import {
  UserProfileResponseDto,
  UpdateUserProfileRequestDto,
} from '../../../api/user/user-profile.http-data-source';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
})
export class UserProfile implements OnInit {
  private readonly apiBaseUrl = inject(API_BASE_URL);
  private readonly backendOrigin = this.apiBaseUrl.replace(/\/api\/?$/, '');
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  userId!: number;

  profile: UserProfileResponseDto | null = null;

  loading = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;

  form: UpdateUserProfileRequestDto = {
    firstName: '',
    lastName: '',
    address: '',
    phoneNumber: '',
    profileImageUrl: '',
  };

  constructor(private api: UserProfileHttpDataSource) {}

  ngOnInit(): void {
    const id = this.authStore.getCurrentUserId();
    if (!id) {
      this.router.navigate(['/login']);
      return;
    }
    this.userId = id;
    this.loadProfile();
  }

  get avatarUrl(): string {
    const candidate = this.form.profileImageUrl || this.profile?.profileImageUrl || '';
    return this.resolveImageUrl(candidate);
  }

  onAvatarError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'avatar.svg';
  }

  loadProfile(keepSuccessNotice = false): void {
    this.loading = true;
    this.errorMsg = null;
    if (!keepSuccessNotice) this.successMsg = null;

    this.api.getProfile(this.userId).subscribe({
      next: (res) => {
        this.profile = res;

        this.form.firstName = res.firstName ?? '';
        this.form.lastName = res.lastName ?? '';
        this.form.address = res.address ?? '';
        this.form.phoneNumber = res.phoneNumber ?? '';

        this.form.profileImageUrl = this.resolveImageUrl(res.profileImageUrl ?? '');

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

    const backendProfileImageUrl = this.toBackendUrl(this.form.profileImageUrl ?? '');

    const payload: UpdateUserProfileRequestDto = {
      firstName: this.form.firstName?.trim(),
      lastName: this.form.lastName?.trim(),
      address: this.form.address?.trim(),
      phoneNumber: this.form.phoneNumber?.trim(),
      profileImageUrl:
        backendProfileImageUrl && backendProfileImageUrl !== 'avatar.svg'
          ? backendProfileImageUrl
          : undefined,
    };

    this.api.updateProfile(this.userId, payload).subscribe({
      next: () => {
        this.loading = false;
        this.successMsg = 'Profile successfully updated.';
        this.loadProfile(true);
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Cannot update profile.';
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

    this.api.uploadProfileImage(this.userId, file).subscribe({
      next: (res) => {
        this.form.profileImageUrl = this.resolveImageUrl(res.profileImageUrl);
        this.loading = false;
        this.successMsg = 'Image uploaded.';
      },
      error: () => {
        this.loading = false;
        this.errorMsg = 'Image upload failed.';
      },
    });

    input.value = '';
  }

  private resolveImageUrl(url: string): string {
    const u = (url || '').trim();
    if (!u) return 'avatar.svg';

    if (/^https?:\/\//i.test(u)) return u;

    if (u.startsWith('/')) return `${this.backendOrigin}${u}`;

    return u;
  }

  private toBackendUrl(url: string): string {
    const u = (url || '').trim();
    if (!u) return '';
    return u.startsWith(this.backendOrigin) ? u.replace(this.backendOrigin, '') : u;
  }
}
