import { CommonModule, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';

import {
  AdminProfileChangeRequestsHttpDataSource,
  AdminProfileChangeRequestDto
} from '../../../api/admin/admin-profile-change-requests.http-data-source';

import { DriverProfileHttpDataSource } from '../../../api/driver/driver-profile.http-data-source';
import { UserProfileResponseDto } from '../../../api/driver/models/driver-profile.models';

type ChangeRow = {
  field: string;
  kind: 'text' | 'image';
  current?: string;
  requested?: string;
  currentUrl?: string;
  requestedUrl?: string;
};


@Component({
  selector: 'app-admin-update-request-details',
  standalone: true,
  imports: [CommonModule, DatePipe, RouterLink],
  templateUrl: './admin-update-request-details.html',
  styleUrl: './admin-update-request-details.css',
})
export class AdminUpdateRequestDetails implements OnInit {
  requestId: number;

  isLoading = false;
  errorMsg = '';

  request?: AdminProfileChangeRequestDto;
  current?: UserProfileResponseDto;

  changes: ChangeRow[] = [];

  acting: 'approve' | 'reject' | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminApi: AdminProfileChangeRequestsHttpDataSource,
    private driverApi: DriverProfileHttpDataSource
  ) {
    const id = Number(this.route.snapshot.paramMap.get('requestId') ?? '0');
    this.requestId = Number.isFinite(id) ? id : 0;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    if (!this.requestId) {
      this.errorMsg = 'Invalid request id.';
      return;
    }

    this.isLoading = true;
    this.errorMsg = '';
    this.request = undefined;
    this.current = undefined;
    this.changes = [];

    this.adminApi.get(this.requestId).pipe(
      switchMap((req) =>
        forkJoin({
          req: of(req),
          profile: this.driverApi.getProfile(req.driverId)
        })
      )
    ).subscribe({
      next: ({ req, profile }) => {
        this.request = req;
        this.current = profile?.driver as any;
        this.changes = this.buildChanges(req, this.current);
        this.isLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMsg = this.extractMsg(err, 'Failed to load request details.');
      }
    });
  }

  approve(): void {
    if (!this.request) return;
    if (this.acting) return;

    if (String(this.request.status).toUpperCase() !== 'PENDING') {
      this.errorMsg = 'This request is not pending anymore.';
      return;
    }

    if (!confirm('Approve this profile update request?')) return;

    this.errorMsg = '';
    this.acting = 'approve';

    this.adminApi.approve(this.requestId).subscribe({
      next: () => {
        this.acting = null;
        this.router.navigate(['/admin/update-requests']);
      },
      error: (err: HttpErrorResponse) => {
        this.acting = null;
        this.errorMsg = this.extractMsg(err, 'Failed to approve the request.');
      }
    });
  }

  reject(): void {
    if (!this.request) return;
    if (this.acting) return;

    if (String(this.request.status).toUpperCase() !== 'PENDING') {
      this.errorMsg = 'This request is not pending anymore.';
      return;
    }

    if (!confirm('Reject this profile update request?')) return;

    this.errorMsg = '';
    this.acting = 'reject';

    this.adminApi.reject(this.requestId).subscribe({
      next: () => {
        this.acting = null;
        this.router.navigate(['/admin/update-requests']);
      },
      error: (err: HttpErrorResponse) => {
        this.acting = null;
        this.errorMsg = this.extractMsg(err, 'Failed to reject the request.');
      }
    });
  }

  private buildChanges(req: AdminProfileChangeRequestDto, cur?: UserProfileResponseDto): ChangeRow[] {
    const out: ChangeRow[] = [];
    if (!cur) return out;

    const addText = (field: string, requested: any, current: any) => {
      const r = this.norm(requested);
      if (!r) return;
      const c = this.norm(current);
      if (r !== c) out.push({ field, kind: 'text', current: c || '—', requested: r });
    };

    addText('First name', req.firstName, cur.firstName);
    addText('Last name', req.lastName, cur.lastName);
    addText('Address', req.address, cur.address);
    addText('Phone number', req.phoneNumber, cur.phoneNumber);

    const rImg = this.norm(req.profileImageUrl);
    const cImg = this.norm(cur.profileImageUrl);
    if (rImg && rImg !== cImg) {
      out.push({
        field: 'Profile image',
        kind: 'image',
        currentUrl: cImg || '',
        requestedUrl: rImg
      });
    }

    return out;
  }

  private norm(v: any): string {
    if (v === null || v === undefined) return '';
    return String(v).trim();
  }

  private extractMsg(err: HttpErrorResponse, fallback: string): string {
    return (
      (err as any)?.error?.message ||
      (typeof (err as any)?.error === 'string' ? (err as any).error : '') ||
      fallback
    );
  }
}
