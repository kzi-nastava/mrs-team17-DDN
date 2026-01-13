import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, timer } from 'rxjs';
import { tap } from 'rxjs/operators';
import { InconsistencyReport, LatLng, TrackingState } from './ride-tracking.models';
import { RideTrackingDataSource } from './ride-tracking.datasource';

@Injectable()
export class RideTrackingMockDataSource implements RideTrackingDataSource {
  private pickup: LatLng = { lat: 45.2671, lng: 19.8335 };
  private destination: LatLng = { lat: 45.2558, lng: 19.8452 };
  private startCar: LatLng = { lat: 45.2692, lng: 19.8298 };

  private route: LatLng[] = [
    ...this.interpolate(this.startCar, this.pickup, 20),
    ...this.interpolate(this.pickup, this.destination, 40),
  ];

  private idx = 0;
  private started = false;

  private state$ = new BehaviorSubject<TrackingState>(
    this.buildState(this.route[0], 0)
  );

  watchTracking(_: number): Observable<TrackingState> {
    if (!this.started) {
      this.started = true;
      timer(0, 1000)
        .pipe(
          tap(() => {
            if (this.idx >= this.route.length - 1) return;
            this.idx++;
            const pos = this.route[this.idx];
            this.state$.next(this.buildState(pos, this.idx));
          })
        )
        .subscribe();
    }
    return this.state$.asObservable();
  }

  submitInconsistency(rideId: number, text: string): Observable<void> {
    const t = text.trim();
    if (!t) return of(void 0);

    const key = this.reportsKey(rideId);
    const current = this.readReports(rideId);
    const next: InconsistencyReport[] = [
      { rideId, text: t, createdAt: new Date().toISOString() },
      ...current,
    ];
    localStorage.setItem(key, JSON.stringify(next));
    return of(void 0);
  }

  listInconsistencies(rideId: number): Observable<InconsistencyReport[]> {
    return of(this.readReports(rideId));
  }

  private buildState(pos: LatLng, idx: number): TrackingState {
    const remainingMeters = this.totalRemainingMeters(pos, this.route, idx);
    const distanceKm = Math.max(0, Math.round((remainingMeters / 1000) * 10) / 10);

    const etaSec = remainingMeters / 6;
    const etaMinutes = Math.max(0, Math.min(60, Math.ceil(etaSec / 60)));

    let status = 'Driver approaching';
    if (idx >= 18 && idx < 25) status = 'Waiting at pickup';
    if (idx >= 25 && idx < this.route.length - 1) status = 'On trip';
    if (idx >= this.route.length - 1) status = 'Arrived';

    return {
      car: pos,
      pickup: this.pickup,
      destination: this.destination,
      etaMinutes,
      distanceKm,
      status,
    };
  }

  private reportsKey(rideId: number): string {
    return `ride_inconsistency_reports_${rideId}`;
  }

  private readReports(rideId: number): InconsistencyReport[] {
    const raw = localStorage.getItem(this.reportsKey(rideId));
    if (!raw) return [];
    try {
      const parsed = JSON.parse(raw) as InconsistencyReport[];
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  private interpolate(a: LatLng, b: LatLng, steps: number): LatLng[] {
    const pts: LatLng[] = [];
    for (let i = 0; i <= steps; i++) {
      const t = i / steps;
      pts.push({
        lat: a.lat + (b.lat - a.lat) * t,
        lng: a.lng + (b.lng - a.lng) * t,
      });
    }
    return pts;
  }

  private totalRemainingMeters(current: LatLng, route: LatLng[], index: number): number {
    let meters = 0;
    let prev = current;
    for (let i = index + 1; i < route.length; i++) {
      meters += this.haversineMeters(prev, route[i]);
      prev = route[i];
    }
    return meters;
  }

  private haversineMeters(a: LatLng, b: LatLng): number {
    const R = 6371000;
    const toRad = (v: number) => (v * Math.PI) / 180;

    const dLat = toRad(b.lat - a.lat);
    const dLng = toRad(b.lng - a.lng);
    const lat1 = toRad(a.lat);
    const lat2 = toRad(b.lat);

    const sinDLat = Math.sin(dLat / 2);
    const sinDLng = Math.sin(dLng / 2);

    const x =
      sinDLat * sinDLat +
      Math.cos(lat1) * Math.cos(lat2) * sinDLng * sinDLng;
    const c = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
    return R * c;
  }
}
