import { AfterViewInit, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import * as L from 'leaflet';

import {
  CreateRideRequestDto,
  CreateRideResponseDto,
  RideOrderApiService,
  RidePointRequestDto
} from '../../../api/user/ride-order.http-data-source';

@Component({
  selector: 'app-user-order-ride',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-order-ride.html',
  styleUrl: './user-order-ride.css',
})
export class UserOrderRide implements AfterViewInit {
  private map!: L.Map;

  requesterUserId = 3003;

  orderType: 'now' | 'schedule' = 'now';
  scheduledAtLocal = '';

  vehicleType: 'standard' | 'luxury' | 'van' = 'standard';
  babyTransport = false;
  petTransport = false;

  start: RidePointRequestDto = { address: '', lat: 45.2671, lng: 19.8335 };
  destination: RidePointRequestDto = { address: '', lat: 45.2668, lng: 19.8339 };

  checkpointInput = '';
  checkpoints: RidePointRequestDto[] = [];

  userEmailInput = '';
  linkedUsersDisplay: string[] = [];
  linkedUsersPayload: string[] = [];
  private guestCounter = 1;

  isSubmitting = false;
  successMsg = '';
  errorMsg = '';

  isGeocoding = false;

  private startMarker?: L.CircleMarker;
  private destMarker?: L.CircleMarker;
  private checkpointMarkers: L.CircleMarker[] = [];
  private checkpointCounter = 1;

  constructor(private api: RideOrderApiService, private http: HttpClient) {}

  ngAfterViewInit(): void {
    this.map = L.map('orderMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.applyPrefillFromNavState();

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;

      if (!this.startMarker) {
        void this.setStartFromMap(lat, lng);
        return;
      }

      if (!this.destMarker) {
        void this.setDestinationFromMap(lat, lng);
        return;
      }

      void this.addCheckpointFromMap(lat, lng);
    });

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  private applyPrefillFromNavState(): void {
    const st: any = history.state?.prefillRide;
    if (!st?.start || !st?.destination) return;

    this.start = { ...st.start };
    this.destination = { ...st.destination };
    this.checkpoints = Array.isArray(st.checkpoints) ? st.checkpoints.map((c: any) => ({ ...c })) : [];

    this.drawStartMarker(this.start.lat, this.start.lng);
    this.drawDestMarker(this.destination.lat, this.destination.lng);

    this.resetCheckpointMarkers();
    for (const cp of this.checkpoints) {
      this.drawCheckpointMarker(cp.lat, cp.lng);
    }

    const pts: L.LatLngExpression[] = [
      [this.start.lat, this.start.lng],
      ...this.checkpoints.map(c => [c.lat, c.lng] as L.LatLngExpression),
      [this.destination.lat, this.destination.lng],
    ];

    if (pts.length >= 2) {
      const line = L.polyline(pts);
      this.map.fitBounds(line.getBounds(), { padding: [30, 30] });
    }
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

  private drawCheckpointMarker(lat: number, lng: number): void {
    const m = L.circleMarker([lat, lng], {
      radius: 7,
      color: '#3498db',
      fillColor: '#3498db',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);

    this.checkpointMarkers.push(m);
  }

  private resetCheckpointMarkers(): void {
    this.checkpointMarkers.forEach(m => m.remove());
    this.checkpointMarkers = [];
    this.checkpointCounter = 1;
  }

  private async reverseGeocode(lat: number, lng: number): Promise<string> {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`;

    try {
      const res: any = await firstValueFrom(
        this.http.get(url, { headers: { Accept: 'application/json' } })
      );

      const display = (res?.display_name || '').trim();
      if (display) return display;

      const a = res?.address || {};
      const road = (a.road || a.pedestrian || a.footway || '').trim();
      const house = (a.house_number || '').trim();
      const city = (a.city || a.town || a.village || '').trim();

      const line1 = [road, house].filter(Boolean).join(' ');
      const line2 = city ? `, ${city}` : '';
      const composed = (line1 + line2).trim();

      return composed || 'Picked location';
    } catch {
      return 'Picked location';
    }
  }

  private async setStartFromMap(lat: number, lng: number): Promise<void> {
    this.start.lat = lat;
    this.start.lng = lng;

    this.isGeocoding = true;
    this.start.address = await this.reverseGeocode(lat, lng);
    this.isGeocoding = false;

    this.drawStartMarker(lat, lng);
  }

  private async setDestinationFromMap(lat: number, lng: number): Promise<void> {
    this.destination.lat = lat;
    this.destination.lng = lng;

    this.isGeocoding = true;
    this.destination.address = await this.reverseGeocode(lat, lng);
    this.isGeocoding = false;

    this.drawDestMarker(lat, lng);
  }

  private async addCheckpointFromMap(lat: number, lng: number): Promise<void> {
    this.isGeocoding = true;
    const addr = await this.reverseGeocode(lat, lng);
    this.isGeocoding = false;

    this.checkpoints.push({ address: addr, lat, lng });
    this.drawCheckpointMarker(lat, lng);
  }

  addCheckpoint(): void {
    const v = (this.checkpointInput || '').trim();
    if (!v) return;

    const center = this.map?.getCenter();
    const lat = center ? center.lat : 45.2671;
    const lng = center ? center.lng : 19.8335;

    this.checkpoints.push({ address: v, lat, lng });
    this.checkpointInput = '';
  }

  addUser(): void {
    const email = (this.userEmailInput || '').trim();

    if (email.length === 0) {
      const label = `Guest ${this.guestCounter++}`;
      this.linkedUsersDisplay.push(label);
      this.linkedUsersPayload.push('');
      return;
    }

    this.linkedUsersDisplay.push(email);
    this.linkedUsersPayload.push(email);
    this.userEmailInput = '';
  }

  private toScheduledAtIso(): string | null {
    if (this.orderType !== 'schedule') return null;
    if (!this.scheduledAtLocal) return null;

    const d = new Date(this.scheduledAtLocal);
    if (isNaN(d.getTime())) return null;
    return d.toISOString();
  }

  placeOrder(form: NgForm): void {
    this.successMsg = '';
    this.errorMsg = '';

    if (this.isSubmitting) return;

    if (!this.start.address.trim() || !this.destination.address.trim()) {
      this.errorMsg = 'Please enter From/To address (and optionally pick coordinates on the map).';
      return;
    }

    if (this.orderType === 'schedule') {
      const iso = this.toScheduledAtIso();
      if (!iso) {
        this.errorMsg = 'Please select a valid date/time for schedule order.';
        return;
      }
    }

    const payload: CreateRideRequestDto = {
      requesterUserId: this.requesterUserId,
      orderType: this.orderType,
      scheduledAt: this.toScheduledAtIso(),

      start: this.start,
      destination: this.destination,
      checkpoints: this.checkpoints,

      linkedUsers: this.linkedUsersPayload,

      vehicleType: this.vehicleType,
      babyTransport: this.babyTransport,
      petTransport: this.petTransport
    };

    this.isSubmitting = true;

    this.api.createRide(payload).subscribe({
      next: (resp: CreateRideResponseDto) => {
        this.successMsg = `Order placed. Price: ${resp.price}RSD.`;
        this.isSubmitting = false;

        form.resetForm({
          orderType: 'now',
          vehicleType: 'standard',
          babyTransport: false,
          petTransport: false
        });

        this.orderType = 'now';
        this.scheduledAtLocal = '';

        this.checkpoints = [];
        this.startMarker?.remove();
        this.startMarker = undefined;

        this.destMarker?.remove();
        this.destMarker = undefined;

        this.resetCheckpointMarkers();

        this.checkpointInput = '';

        this.linkedUsersDisplay = [];
        this.linkedUsersPayload = [];
        this.userEmailInput = '';
        this.guestCounter = 1;
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        const msg =
          err?.error?.message ||
          (typeof err?.error === 'string' ? err.error : '') ||
          'Request failed. Please check input and try again.';
        this.errorMsg = msg;
      }
    });
  }
}
