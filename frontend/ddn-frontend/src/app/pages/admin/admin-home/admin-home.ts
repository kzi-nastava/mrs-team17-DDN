import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AdminUsersHttpDataSource } from '../../../api/admin/admin-users.http.datasource';
import { AdminUserStatusDto } from '../../../api/admin/models/admin-user-status.model';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-home.html',
  styleUrl: './admin-home.css',
})
export class AdminHome implements OnInit {
  private readonly usersDs = inject(AdminUsersHttpDataSource);

  drivers: AdminUserStatusDto[] = [];
  passengers: AdminUserStatusDto[] = [];

  loadingDrivers = false;
  loadingPassengers = false;
  errorDrivers = '';
  errorPassengers = '';

  modalOpen = false;
  modalUser: AdminUserStatusDto | null = null;
  modalBlocked = true;
  modalReason = '';
  modalSaving = false;
  modalError = '';

  ngOnInit(): void {
    this.reloadAll();
  }

  reloadAll(): void {
    this.reloadDrivers();
    this.reloadPassengers();
  }

  reloadDrivers(): void {
    this.loadingDrivers = true;
    this.errorDrivers = '';
    this.usersDs.listUsersWithStatus('DRIVER', '', 500).subscribe({
      next: (res) => {
        this.drivers = (res || []).slice();
        this.loadingDrivers = false;
      },
      error: () => {
        this.loadingDrivers = false;
        this.errorDrivers = 'Failed to load drivers.';
      },
    });
  }

  reloadPassengers(): void {
    this.loadingPassengers = true;
    this.errorPassengers = '';
    this.usersDs.listUsersWithStatus('PASSENGER', '', 500).subscribe({
      next: (res) => {
        this.passengers = (res || []).slice();
        this.loadingPassengers = false;
      },
      error: () => {
        this.loadingPassengers = false;
        this.errorPassengers = 'Failed to load passengers.';
      },
    });
  }

  openBlockModal(user: AdminUserStatusDto, blocked: boolean): void {
    this.modalUser = user;
    this.modalBlocked = blocked;
    this.modalReason = (blocked ? (user.blockReason ?? '') : '').toString();
    this.modalError = '';
    this.modalOpen = true;
  }

  closeModal(): void {
    if (this.modalSaving) return;
    this.modalOpen = false;
    this.modalUser = null;
    this.modalReason = '';
    this.modalError = '';
  }

  saveModal(): void {
    if (!this.modalUser) return;
    if (this.modalSaving) return;

    this.modalSaving = true;
    this.modalError = '';

    const userId = Number(this.modalUser.id);

    this.usersDs
      .setBlockStatus(userId, {
        blocked: this.modalBlocked,
        blockReason: this.modalBlocked ? this.modalReason : null,
      })
      .subscribe({
        next: (updated) => {
          this.applyUpdatedUser(updated);
          this.modalSaving = false;
          this.closeModal();
        },
        error: (err) => {
          const msg =
            err?.error?.message ||
            (typeof err?.error === 'string' ? err.error : '') ||
            'Failed to save.';
          this.modalSaving = false;
          this.modalError = msg;
        },
      });
  }

  quickUnblock(user: AdminUserStatusDto): void {
    const userId = Number(user.id);
    this.usersDs.setBlockStatus(userId, { blocked: false, blockReason: null }).subscribe({
      next: (updated) => this.applyUpdatedUser(updated),
      error: () => {
        if ((user.role || '').toUpperCase() === 'DRIVER') this.reloadDrivers();
        else this.reloadPassengers();
      },
    });
  }

  private applyUpdatedUser(updated: AdminUserStatusDto): void {
    const role = (updated.role || '').toUpperCase();

    if (role === 'DRIVER') {
      this.drivers = this.drivers.map((u) => (u.id === updated.id ? updated : u));
      return;
    }

    if (role === 'PASSENGER') {
      this.passengers = this.passengers.map((u) => (u.id === updated.id ? updated : u));
      return;
    }

    this.reloadAll();
  }

  fullName(u: AdminUserStatusDto): string {
    return `${u.firstName || ''} ${u.lastName || ''}`.trim();
  }

  reasonPreview(u: AdminUserStatusDto): string {
    const r = (u.blockReason ?? '').toString().trim();
    if (!r) return '';
    return r.length > 90 ? r.slice(0, 90).trimEnd() + '…' : r;
  }
}
