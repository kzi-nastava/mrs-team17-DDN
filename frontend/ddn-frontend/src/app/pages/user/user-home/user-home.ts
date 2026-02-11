import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http';
import { Subject, of, timer } from 'rxjs';
import { catchError, exhaustMap, takeUntil } from 'rxjs/operators';

import { ActiveVehiclesHttpDataSource } from '../../landing/active-vehicles.http.datasource';
import { API_BASE_URL } from '../../../app.config';

type UiVehicle = { id: number; lat: number; lng: number; status: 'free' | 'busy' };
type PendingRideResponse = { rideId: number };

const VEHICLE_POLL_MS = 800;

@Component({
  selector: 'app-user-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-home.html',
  styleUrl: './user-home.css',
})
export class UserHome implements AfterViewInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);

  private map!: L.Map;
  private ds = inject(ActiveVehiclesHttpDataSource);

  private http = inject(HttpClient);
  private baseUrl = inject(API_BASE_URL);

  private markersLayer = L.layerGroup();

  private destroy$ = new Subject<void>();

  private markersById = new Map<number, L.CircleMarker>();
  private lastPosById = new Map<number, { lat: number; lng: number }>();
  private animFrameById = new Map<number, number>();

  vehicles: UiVehicle[] = [];

  pendingRideId: number | null = null;

  get freeCount(): number {
    return this.vehicles.filter(v => v.status === 'free').length;
  }

  get busyCount(): number {
    return this.vehicles.filter(v => v.status === 'busy').length;
  }

  ngAfterViewInit(): void {
    this.map = L.map('map', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.markersLayer.addTo(this.map);

    this.startVehiclePolling();
    this.loadPendingRating();

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    for (const raf of this.animFrameById.values()) {
      try { cancelAnimationFrame(raf); } catch {}
    }
    this.animFrameById.clear();

    if (this.map) this.map.remove();
  }

  private startVehiclePolling(): void {
    timer(0, VEHICLE_POLL_MS)
      .pipe(
        takeUntil(this.destroy$),
        exhaustMap(() =>
          this.ds.getActiveVehicles().pipe(
            catchError((err) => {
              console.error('Failed to load active vehicles', err);
              return of(null);
            })
          )
        )
      )
      .subscribe((list) => {
        if (!list) return;

        this.vehicles = list.map(v => ({
          id: v.id,
          lat: v.latitude,
          lng: v.longitude,
          status: v.busy ? 'busy' : 'free',
        }));

        this.syncMarkers(this.vehicles);
        this.cdr.detectChanges();
      });
  }

  private loadPendingRating(): void {
    this.http
      .get<PendingRideResponse>(`${this.baseUrl}/rides/rate/pending`)
      .pipe(
        catchError((err) => {
          if (err?.status === 404) return of(null);
          console.error('Failed to load pending rating', err);
          return of(null);
        })
      )
      .subscribe((res) => {
        const id = Number(res?.rideId);
        this.pendingRideId = Number.isFinite(id) && id > 0 ? id : null;
        this.cdr.detectChanges();
      });
  }

  private syncMarkers(vehicles: UiVehicle[]): void {
    const seen = new Set<number>();

    for (const v of vehicles) {
      seen.add(v.id);

      const color = v.status === 'free' ? '#2ecc71' : '#e74c3c';
      const existing = this.markersById.get(v.id);

      if (!existing) {
        const marker = L.circleMarker([v.lat, v.lng], {
          radius: 8,
          color,
          fillColor: color,
          fillOpacity: 0.9,
          weight: 2,
        }).addTo(this.markersLayer);

        marker.bindPopup(`Vehicle ${v.id} — ${v.status.toUpperCase()}`);
        marker.on('click', () => this.map.setView([v.lat, v.lng], 15, { animate: true }));

        this.markersById.set(v.id, marker);
        this.lastPosById.set(v.id, { lat: v.lat, lng: v.lng });
        continue;
      }

      existing.setStyle({ color, fillColor: color });
      existing.getPopup()?.setContent(`Vehicle ${v.id} — ${v.status.toUpperCase()}`);

      this.animateMarkerTo(v.id, existing, { lat: v.lat, lng: v.lng }, Math.max(200, Math.floor(VEHICLE_POLL_MS * 0.85)));
    }

    for (const [id, marker] of this.markersById.entries()) {
      if (seen.has(id)) continue;

      const raf = this.animFrameById.get(id);
      if (raf != null) {
        try { cancelAnimationFrame(raf); } catch {}
        this.animFrameById.delete(id);
      }

      try { marker.remove(); } catch {}
      this.markersById.delete(id);
      this.lastPosById.delete(id);
    }
  }

  private animateMarkerTo(
    id: number,
    marker: L.CircleMarker,
    target: { lat: number; lng: number },
    durationMs: number
  ): void {
    const from = this.lastPosById.get(id) ?? target;

    const prev = this.animFrameById.get(id);
    if (prev != null) {
      try { cancelAnimationFrame(prev); } catch {}
      this.animFrameById.delete(id);
    }

    const start = performance.now();

    const step = (now: number) => {
      const t = Math.min(1, (now - start) / durationMs);

      const lat = from.lat + (target.lat - from.lat) * t;
      const lng = from.lng + (target.lng - from.lng) * t;

      marker.setLatLng([lat, lng]);

      if (t < 1) {
        this.animFrameById.set(id, requestAnimationFrame(step));
      } else {
        this.animFrameById.delete(id);
        this.lastPosById.set(id, { ...target });
      }
    };

    this.animFrameById.set(id, requestAnimationFrame(step));
  }
}
