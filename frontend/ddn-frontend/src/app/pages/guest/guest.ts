import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';
import { Subject, firstValueFrom, of } from 'rxjs';
import { catchError, finalize, takeUntil } from 'rxjs/operators';

import { PublicNavbarComponent } from '../../components/public-navbar/public-navbar';
import { RoutingHttpDataSource, RoutePreviewResponse } from '../../api/routing/routing-http.datasource';

type Point = { address: string; lat: number; lng: number };

// privremena formula
const PRICE_BASE_RSD = 250;
const PRICE_PER_KM_RSD = 80;
const PRICE_PER_MIN_RSD = 10;

@Component({
  selector: 'app-guest',
  standalone: true,
  imports: [CommonModule, FormsModule, PublicNavbarComponent],
  templateUrl: './guest.html',
  styleUrl: './guest.css',
})
export class GuestComponent implements AfterViewInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);
  private http = inject(HttpClient);
  private routing = inject(RoutingHttpDataSource);

  private map!: L.Map;
  private destroy$ = new Subject<void>();

  start: Point = { address: '', lat: 0, lng: 0 };
  destination: Point = { address: '', lat: 0, lng: 0 };

  hasStart = false;
  hasDestination = false;

  isGeocoding = false;
  previewLoading = false;

  previewEtaMinutes: number | null = null;
  previewDistanceKm: number | null = null;
  previewPriceRsd: number | null = null;

  private startMarker?: L.CircleMarker;
  private destMarker?: L.CircleMarker;
  private previewLine: L.Polyline | null = null;

  ngAfterViewInit(): void {
    this.map = L.map('guestMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;

      if (!this.hasStart) {
        void this.pickStart(lat, lng);
        return;
      }

      if (!this.hasDestination) {
        void this.pickDestination(lat, lng);
        return;
      }

      // treći klik reset
      this.resetAll();
      void this.pickStart(lat, lng);
    });

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    try { this.map?.remove(); } catch {}
  }

  async pickStart(lat: number, lng: number): Promise<void> {
    this.hasStart = true;
    this.start.lat = lat;
    this.start.lng = lng;

    this.drawStartMarker(lat, lng);

    this.isGeocoding = true;
    this.start.address = await this.reverseGeocode(lat, lng);
    this.isGeocoding = false;

    this.cdr.detectChanges();
  }

  async pickDestination(lat: number, lng: number): Promise<void> {
    this.hasDestination = true;
    this.destination.lat = lat;
    this.destination.lng = lng;

    this.drawDestMarker(lat, lng);

    this.isGeocoding = true;
    this.destination.address = await this.reverseGeocode(lat, lng);
    this.isGeocoding = false;

    this.cdr.detectChanges();
  }

  showEstimate(): void {
    if (!this.hasStart || !this.hasDestination) return;

    const pts = [
      { lat: this.start.lat, lng: this.start.lng },
      { lat: this.destination.lat, lng: this.destination.lng },
    ];

    this.previewLoading = true;

    this.routing.previewRoute(pts)
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Guest previewRoute failed', err);
          return of(null);
        }),
        finalize(() => {
          this.previewLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe((res: RoutePreviewResponse | null) => {
        if (!res || !res.route || res.route.length < 2) {
          this.clearPreview();
          return;
        }

        this.previewEtaMinutes = res.etaMinutes ?? null;
        this.previewDistanceKm = res.distanceKm ?? null;

        if (this.previewEtaMinutes != null && this.previewDistanceKm != null) {
          const price =
            PRICE_BASE_RSD +
            this.previewDistanceKm * PRICE_PER_KM_RSD +
            this.previewEtaMinutes * PRICE_PER_MIN_RSD;

          this.previewPriceRsd = Math.round(price);
        } else {
          this.previewPriceRsd = null;
        }

        this.drawPreviewLine(res.route);
      });
  }

  resetAll(): void {
    this.hasStart = false;
    this.hasDestination = false;

    this.start = { address: '', lat: 0, lng: 0 };
    this.destination = { address: '', lat: 0, lng: 0 };

    this.startMarker?.remove();
    this.destMarker?.remove();
    this.startMarker = undefined;
    this.destMarker = undefined;

    this.clearPreview();
    this.cdr.detectChanges();
  }

  private clearPreview(): void {
    this.previewLine?.remove();
    this.previewLine = null;

    this.previewEtaMinutes = null;
    this.previewDistanceKm = null;
    this.previewPriceRsd = null;
  }

  private drawStartMarker(lat: number, lng: number): void {
    this.startMarker?.remove();
    this.startMarker = L.circleMarker([lat, lng], {
      radius: 8,
      color: '#2ecc71',
      fillColor: '#2ecc71',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);
  }

  private drawDestMarker(lat: number, lng: number): void {
    this.destMarker?.remove();
    this.destMarker = L.circleMarker([lat, lng], {
      radius: 8,
      color: '#e74c3c',
      fillColor: '#e74c3c',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);
  }

  private drawPreviewLine(route: { lat: number; lng: number }[]): void {
    this.previewLine?.remove();

    this.previewLine = L.polyline(
      route.map(p => [p.lat, p.lng] as [number, number]),
      { weight: 5, opacity: 0.9 }
    ).addTo(this.map);

    try {
      this.map.fitBounds(this.previewLine.getBounds(), { padding: [20, 20] });
    } catch {}
  }

  private async reverseGeocode(lat: number, lng: number): Promise<string> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;
    try {
      const res: any = await firstValueFrom(
        this.http.get(url, { headers: { Accept: 'application/json' } })
      );

      const display = (res?.display_name || '').trim();
      return display || 'Picked location';
    } catch {
      return 'Picked location';
    }
  }
}
