import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AdminReportsHttpDataSource } from '../../../api/admin/admin-reports.http.datasource';
import { AdminUsersHttpDataSource } from '../../../api/admin/admin-users.http.datasource';
import { AdminUserOptionDto } from '../../../api/admin/models/admin-user-option.model';

import { RideStatsPointDto, RideStatsReportResponseDto } from '../../../api/user/models/reports.models';

type MetricKey = 'rides' | 'kilometers' | 'money';

type ChartBar = {
  date: string;
  label: string;
  value: number;
  pct: number;
  tooltip: string;
};

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reports.html',
  styleUrl: './admin-reports.css',
})
export class AdminReports implements OnInit {
  private readonly reportsDs = inject(AdminReportsHttpDataSource);
  private readonly usersDs = inject(AdminUsersHttpDataSource);

  fromDate = '';
  toDate = '';

  role: 'DRIVER' | 'PASSENGER' = 'DRIVER';
  scope: 'ALL' | 'USER' = 'ALL';

  users: AdminUserOptionDto[] = [];
  usersLoading = false;
  selectedUserId: number | null = null;

  isLoading = false;
  errorMsg = '';

  report: RideStatsReportResponseDto | null = null;

  metric: MetricKey = 'rides';
  bars: ChartBar[] = [];

  ngOnInit(): void {
    const today = new Date();
    this.toDate = this.toDateInput(today);
    this.fromDate = this.toDateInput(this.addDays(today, -6));

    this.load();
  }

  onRoleChange(): void {
    if (this.scope === 'USER') {
      this.selectedUserId = null;
      this.loadUsers();
    }
  }

  onScopeChange(): void {
    this.errorMsg = '';
    if (this.scope === 'ALL') {
      this.selectedUserId = null;
      this.users = [];
    } else {
      this.loadUsers();
    }
  }

  private loadUsers(): void {
    this.usersLoading = true;
    this.users = [];
    this.selectedUserId = null;

    this.usersDs.listUsers(this.role, '', 500).subscribe({
      next: (res) => {
        this.users = res || [];
        this.usersLoading = false;
      },
      error: () => {
        this.usersLoading = false;
        this.errorMsg = 'Failed to load users for dropdown.';
      },
    });
  }

  load(): void {
    this.errorMsg = '';

    if (!this.fromDate || !this.toDate) {
      this.errorMsg = 'Please choose both dates.';
      return;
    }
    if (this.fromDate > this.toDate) {
      this.errorMsg = 'From date must be before To date.';
      return;
    }

    let userId: number | undefined = undefined;

    if (this.scope === 'USER') {
      if (!this.selectedUserId) {
        this.errorMsg = 'Please select a user.';
        return;
      }
      userId = this.selectedUserId;
    }

    this.isLoading = true;

    this.reportsDs.getAdminRideReport(this.role, this.fromDate, this.toDate, userId).subscribe({
      next: (res) => {
        this.report = res;
        this.isLoading = false;
        this.rebuildChart();
      },
      error: () => {
        this.isLoading = false;
        this.report = null;
        this.bars = [];
        this.errorMsg = 'Failed to load report.';
      },
    });
  }

  setMetric(m: MetricKey): void {
    this.metric = m;
    this.rebuildChart();
  }

  get isPassenger(): boolean {
    return (this.report?.targetRole ?? this.role) === 'PASSENGER';
  }

  get moneyLabel(): string {
    return this.isPassenger ? 'Total spent' : 'Total earned';
  }

  get moneyPerDayLabel(): string {
    return this.isPassenger ? 'Avg spent/day' : 'Avg earned/day';
  }

  get moneyPerRideLabel(): string {
    return this.isPassenger ? 'Avg spent/ride' : 'Avg earned/ride';
  }

  get chartTitle(): string {
    if (this.metric === 'rides') return 'Number of rides per day';
    if (this.metric === 'kilometers') return 'Kilometers per day';
    return this.isPassenger ? 'Money spent per day' : 'Money earned per day';
  }

  get chartUnit(): string {
    if (this.metric === 'rides') return '';
    if (this.metric === 'kilometers') return 'km';
    return 'RSD';
  }

  formatNumber(n: number, decimals: number): string {
    if (n === null || n === undefined || Number.isNaN(n)) return '0';
    return n.toFixed(decimals);
  }

  userLabel(u: AdminUserOptionDto): string {
    const fn = (u.firstName || '').trim();
    const ln = (u.lastName || '').trim();
    const name = (ln || fn) ? `${ln} ${fn}`.trim() : 'User';
    return `${name} (${u.email}) #${u.id}`;
  }

  private rebuildChart(): void {
    if (!this.report?.points) {
      this.bars = [];
      return;
    }

    const values = this.report.points.map((p) => this.getMetricValue(p, this.metric));
    const max = Math.max(1, ...values);

    this.bars = this.report.points.map((p) => {
      const v = this.getMetricValue(p, this.metric);
      const pct = Math.max(0, Math.min(100, (v / max) * 100));

      return {
        date: p.date,
        label: this.formatShortDate(p.date),
        value: v,
        pct,
        tooltip: this.makeTooltip(p, v),
      };
    });
  }

  private getMetricValue(p: RideStatsPointDto, m: MetricKey): number {
    if (m === 'rides') return Number(p.rides ?? 0);
    if (m === 'kilometers') return Number(p.kilometers ?? 0);
    return Number(p.money ?? 0);
  }

  private makeTooltip(p: RideStatsPointDto, v: number): string {
    const d = p.date;
    if (this.metric === 'rides') return `${d}: ${Math.round(v)} rides`;
    if (this.metric === 'kilometers') return `${d}: ${this.formatNumber(v, 2)} km`;
    return `${d}: ${this.formatNumber(v, 2)} RSD`;
  }

  private formatShortDate(iso: string): string {
    if (!iso || iso.length < 10) return iso;
    const dd = iso.substring(8, 10);
    const mm = iso.substring(5, 7);
    return `${dd}.${mm}`;
  }

  private toDateInput(d: Date): string {
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private addDays(d: Date, days: number): Date {
    const x = new Date(d.getTime());
    x.setDate(x.getDate() + days);
    return x;
  }
}
