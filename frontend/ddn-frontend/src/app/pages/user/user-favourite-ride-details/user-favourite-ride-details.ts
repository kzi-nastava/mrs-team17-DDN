import { CommonModule } from '@angular/common';
import { AfterViewInit, Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import * as L from 'leaflet';

import {
  FavoriteRoutesApiService,
  FavoriteRouteAnyDto,
  isFavoriteRouteNew
} from '../../../api/user/favorite-routes.http-data-source';

type RidePointRequestDto = { address: string; lat: number; lng: number };

type FavouriteRideDetailsVm = {
  id: number;
  start: string;
  checkpoints: string[];
  end: string;
};

@Component({
  selector: 'app-user-favourite-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-favourite-ride-details.html',
  styleUrl: './user-favourite-ride-details.css',
})
export class UserFavouriteRideDetails implements AfterViewInit {
  private map!: L.Map;

  readonly userId = 3003;

  isLoading = false;
  errorMsg = '';

  vm: FavouriteRideDetailsVm = {
    id: 0,
    start: '',
    checkpoints: [],
    end: '',
  };

  private prefillStart?: RidePointRequestDto;
  private prefillDest?: RidePointRequestDto;
  private prefillCps: RidePointRequestDto[] = [];

  private routeLayer?: L.Polyline;
  private markers: L.CircleMarker[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: FavoriteRoutesApiService
  ) {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam ?? '0');
    this.vm = { ...this.vm, id: Number.isFinite(id) ? id : 0 };
  }

  ngAfterViewInit(): void {
    this.map = L.map('favDetailsMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.load();

    setTimeout(() => this.map.invalidateSize(), 0);
  }

  orderAgain(): void {
    if (!this.prefillStart || !this.prefillDest) {
      this.errorMsg = 'Route is not ready for ordering (missing coordinates).';
      return;
    }

    this.router.navigate(['/user/order-ride'], {
      state: {
        prefillRide: {
          start: this.prefillStart,
          destination: this.prefillDest,
          checkpoints: this.prefillCps
        }
      }
    });
  }

  private load(): void {
    this.isLoading = true;
    this.errorMsg = '';

    this.api.getFavorite(this.userId, this.vm.id).subscribe({
      next: (fav: FavoriteRouteAnyDto) => {
        if (isFavoriteRouteNew(fav)) {
          const start = fav.start;
          const dest = fav.destination;
          const stops = fav.stops || [];

          this.vm.start = start.address || '';
          this.vm.end = dest.address || '';
          this.vm.checkpoints = stops.map(s => s.address || '');

          this.prefillStart = { address: start.address, lat: start.lat, lng: start.lng };
          this.prefillDest = { address: dest.address, lat: dest.lat, lng: dest.lng };
          this.prefillCps = stops.map(s => ({ address: s.address, lat: s.lat, lng: s.lng }));

          this.drawRoute([this.prefillStart, ...this.prefillCps, this.prefillDest]);
          this.isLoading = false;
          return;
        }

        this.vm.start = (fav as any).startAddress || '';
        this.vm.end = (fav as any).destinationAddress || '';
        this.vm.checkpoints = ((fav as any).stops || []).slice();

        this.prefillStart = undefined;
        this.prefillDest = undefined;
        this.prefillCps = [];

        this.isLoading = false;
        this.errorMsg =
          'Backend endpoint trenutno vraća favorite bez koordinata (lat/lng), pa ORDER AGAIN ne može automatski prefillovati mapu.';
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        const msg =
          err?.error?.message ||
          (typeof err?.error === 'string' ? err.error : '') ||
          'Failed to load favourite ride details.';
        this.errorMsg = msg;
      }
    });
  }

  private drawRoute(points: RidePointRequestDto[]): void {
    this.clearRoute();

    const latLngs: L.LatLngExpression[] = points.map(p => [p.lat, p.lng]);

    this.routeLayer = L.polyline(latLngs, {
      weight: 5,
      opacity: 0.9,
      color: '#28c200',
    }).addTo(this.map);

    points.forEach((p, idx) => {
      const isStart = idx === 0;
      const isEnd = idx === points.length - 1;

      const m = L.circleMarker([p.lat, p.lng], {
        radius: isStart || isEnd ? 7 : 6,
        color: '#ffffff',
        weight: 2,
        fillColor: isStart ? '#2ecc71' : isEnd ? '#e74c3c' : '#3498db',
        fillOpacity: 0.95,
      })
        .addTo(this.map)
        .bindPopup(isStart ? 'Start' : isEnd ? 'End' : `Checkpoint ${idx}`);

      this.markers.push(m);
    });

    this.map.fitBounds(this.routeLayer.getBounds(), { padding: [30, 30] });
  }

  private clearRoute(): void {
    if (this.routeLayer) {
      this.routeLayer.remove();
      this.routeLayer = undefined;
    }
    this.markers.forEach(m => m.remove());
    this.markers = [];
  }
}
