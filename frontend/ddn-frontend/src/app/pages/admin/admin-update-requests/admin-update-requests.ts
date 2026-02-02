import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import {
  AdminProfileChangeRequestsHttpDataSource,
  AdminProfileChangeRequestDto,
  ProfileChangeRequestStatus,
} from '../../../api/admin/admin-profile-change-requests.http-data-source';

@Component({
  selector: 'app-admin-update-requests',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './admin-update-requests.html',
  styleUrl: './admin-update-requests.css',
})
export class AdminUpdateRequests implements OnInit {
  requests: AdminProfileChangeRequestDto[] = [];
  isLoading = false;
  errorMsg = '';

  constructor(private api: AdminProfileChangeRequestsHttpDataSource) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.api.list('PENDING').subscribe({
      next: (list) => {
        this.requests = (list || []).slice().sort((a, b) => {
          const da = new Date(a.createdAt as any).getTime();
          const db = new Date(b.createdAt as any).getTime();
          return (Number.isFinite(db) ? db : 0) - (Number.isFinite(da) ? da : 0);
        });
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMsg = this.extractMsg(err, 'Failed to load update requests.');
      },
    });
  }

  statusLabel(status: ProfileChangeRequestStatus): string {
    const s = (status || '').toUpperCase();
    if (s === 'PENDING') return 'Pending';
    if (s === 'APPROVED') return 'Approved';
    if (s === 'REJECTED') return 'Rejected';
    return status;
  }

  private extractMsg(err: HttpErrorResponse, fallback: string): string {
    return (
      (err as any)?.error?.message ||
      (typeof (err as any)?.error === 'string' ? (err as any).error : '') ||
      fallback
    );
  }
}
