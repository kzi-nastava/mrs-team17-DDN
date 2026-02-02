import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { API_BASE_URL } from '../../../app.config';
import { AdminProfileHttpDataSource } from '../../../api/admin/admin-profile.http-data-source';
import {
  AdminProfileResponseDto,
  UpdateAdminProfileRequestDto,
} from '../../../api/admin/admin-profile.http-data-source';

@Component({
  selector: 'app-admin-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-profile.html',
  styleUrl: './admin-profile.css',
})
export class AdminProfile implements OnInit {
  private readonly apiBaseUrl = inject(API_BASE_URL); 
  private readonly backendOrigin = this.apiBaseUrl.replace(/\/api\/?$/, ''); 

  adminId = 1001;

  profile: AdminProfileResponseDto | null = null;

  loading = false;
  errorMsg: string | null = null;
  successMsg: string | null = null;

  form: UpdateAdminProfileRequestDto = {
    firstName: '',
    lastName: '',
    address: '',
    phoneNumber: '',
    profileImageUrl: '',
  };

  constructor(private api: AdminProfileHttpDataSource) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  get avatarUrl(): string {
    const candidate = this.form.profileImageUrl || this.profile?.profileImageUrl || '';
    return this.resolveImageUrl(candidate);
  }

  loadProfile(keepSuccessNotice = false): void {
    this.loading = true;
    this.errorMsg = null;
    if (!keepSuccessNotice) this.successMsg = null;

    this.api.getProfile(this.adminId).subscribe({
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

    const payload: UpdateAdminProfileRequestDto = {
      firstName: this.form.firstName?.trim(),
      lastName: this.form.lastName?.trim(),
      address: this.form.address?.trim(),
      phoneNumber: this.form.phoneNumber?.trim(),

      profileImageUrl: this.stripBackendOrigin(this.form.profileImageUrl),
    };

    this.api.updateProfile(this.adminId, payload).subscribe({
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

    this.api.uploadProfileImage(this.adminId, file).subscribe({
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

  onAvatarError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = 'avatar.svg';
  }

  private resolveImageUrl(url: string | null | undefined): string {
    const u = (url ?? '').trim();
    if (!u) return 'avatar.svg';

    if (/^https?:\/\//i.test(u)) return u;

    if (u.startsWith('/')) return `${this.backendOrigin}${u}`;

    return u;
  }

  private stripBackendOrigin(url: string | null | undefined): string {
    const u = (url ?? '').trim();
    if (!u) return '';
    if (u.startsWith(this.backendOrigin)) return u.slice(this.backendOrigin.length);
    return u;
  }
}
