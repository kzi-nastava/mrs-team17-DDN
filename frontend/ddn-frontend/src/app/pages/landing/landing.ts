import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import * as L from 'leaflet';
import { Subject, of, timer } from 'rxjs';
import { catchError, switchMap, takeUntil } from 'rxjs/operators';

import { PublicNavbarComponent } from '../../components/public-navbar/public-navbar';
import { ActiveVehiclesHttpDataSource } from './active-vehicles.http.datasource';

type UiVehicle = { id: number; lat: number; lng: number; status: 'free' | 'busy' };

const VEHICLE_POLL_MS = 800;

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [PublicNavbarComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent implements AfterViewInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);

  private map!: L.Map;
  private ds = inject(ActiveVehiclesHttpDataSource);

  private markersLayer = L.layerGroup();

  private destroy$ = new Subject<void>();

  private markersById = new Map<number, L.CircleMarker>();
  private lastPosById = new Map<number, { lat: number; lng: number }>();
  private animFrameById = new Map<number, number>();

  vehicles: UiVehicle[] = [];

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
        switchMap(() =>
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
