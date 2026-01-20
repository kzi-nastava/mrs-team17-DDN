import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  AdminProfileHttpDataSource,
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
  adminId = 1;

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

  loadProfile(): void {
    this.loading = true;
    this.errorMsg = null;
    this.successMsg = null;

    this.api.getProfile(this.adminId).subscribe({
      next: (res) => {
        this.profile = res;

        this.form.firstName = res.firstName ?? '';
        this.form.lastName = res.lastName ?? '';
        this.form.address = res.address ?? '';
        this.form.phoneNumber = res.phoneNumber ?? '';
        this.form.profileImageUrl = res.profileImageUrl ?? '';

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
      profileImageUrl: (this.form.profileImageUrl || '').replace('http://localhost:8080', '').trim(),
    };

    this.api.updateProfile(this.adminId, payload).subscribe({
      next: () => {
        this.loading = false;
        this.successMsg = 'Profile updated.';
        // this.loadProfile();
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
        this.form.profileImageUrl = `http://localhost:8080${res.profileImageUrl}`;
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
}
