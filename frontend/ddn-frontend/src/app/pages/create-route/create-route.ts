import { Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import * as L from 'leaflet';
import 'leaflet-routing-machine';

import { PublicNavbarComponent } from '../../components/public-navbar/public-navbar';
import { GeocodingService, Suggestion } from '../../services/geocoding.service';

@Component({
  selector: 'app-create-route',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PublicNavbarComponent],
  templateUrl: './create-route.html',
  styleUrls: ['./create-route.css'],
})
export class CreateRouteComponent implements OnDestroy {
  start = '';
  destination = '';

  startSuggestions: Suggestion[] = [];
  destSuggestions: Suggestion[] = [];
  private startTimer: any;
  private destTimer: any;

  private startPoint?: { lat: number; lng: number };
  private destPoint?: { lat: number; lng: number };

  showMap = false;
  isLoading = false;
  errorMsg = '';

  routeDistanceText = '';
  routeTimeText = '';

  distanceKm = 0;
  durationMin = 0;
  ridePrice = 0;

  private map?: L.Map;
  private routing?: any;

  @ViewChild('mapEl') mapEl?: ElementRef<HTMLDivElement>;
  @ViewChild('instructionsEl') instructionsEl?: ElementRef<HTMLDivElement>;

  constructor(private geo: GeocodingService) {}

  // AUTOCOMPLETE
  onStartInput(value: string) {
    clearTimeout(this.startTimer);
    this.start = value;
    this.startPoint = undefined;

    this.startTimer = setTimeout(async () => {
      this.startSuggestions = await this.geo.suggest(this.start);
    }, 200);
  }

  onDestInput(value: string) {
    clearTimeout(this.destTimer);
    this.destination = value;
    this.destPoint = undefined;

    this.destTimer = setTimeout(async () => {
      this.destSuggestions = await this.geo.suggest(this.destination);
    }, 200);
  }

  pickStart(s: Suggestion) {
    this.start = s.label;
    this.startPoint = { lat: s.lat, lng: s.lng };
    this.startSuggestions = [];
  }

  pickDest(s: Suggestion) {
    this.destination = s.label;
    this.destPoint = { lat: s.lat, lng: s.lng };
    this.destSuggestions = [];
  }

  private resetMapState() {
    this.routeDistanceText = '';
    this.routeTimeText = '';
    this.distanceKm = 0;
    this.durationMin = 0;
    this.ridePrice = 0;

    const instr = this.instructionsEl?.nativeElement;
    if (instr) instr.innerHTML = '';

    try {
      if (this.routing && this.map) {
        this.map.removeControl(this.routing);
      }
    } catch {}
    this.routing = undefined;

    if (this.map) {
      try {
        this.map.off();
        this.map.remove();
      } catch {}
      this.map = undefined;
    }
  }

  async submit() {
    this.errorMsg = '';
    this.isLoading = true;

    try {
      this.startSuggestions = [];
      this.destSuggestions = [];

      const a = this.startPoint ?? (await this.geo.geocode(this.start));
      const b = this.destPoint ?? (await this.geo.geocode(this.destination));

      if (!a || !b) {
        this.errorMsg = 'Lokacija nije pronađena u Novom Sadu.';
        return;
      }

      this.resetMapState();
      this.showMap = true;

      setTimeout(() => {
        const mapDiv = this.mapEl?.nativeElement;
        if (!mapDiv) {
          this.errorMsg = 'Mapa nije renderovana.';
          return;
        }
        this.initMapAndRoute(a, b, mapDiv);
      }, 0);
    } catch (e) {
      console.error(e);
      this.errorMsg = 'Greška pri učitavanju rute.';
    } finally {
      this.isLoading = false;
    }
  }

  editRide() {
    this.showMap = false;
    setTimeout(() => this.resetMapState(), 0);
  }

  back() {
    if (this.showMap) {
      this.editRide();
      return;
    }
    window.history.back();
  }

  requestRide() {
    console.log('Ride requested', {
      from: this.start,
      to: this.destination,
      distanceKm: this.distanceKm,
      durationMin: this.durationMin,
      ridePrice: this.ridePrice,
    });
  }

  private initMapAndRoute(
    a: { lat: number; lng: number },
    b: { lat: number; lng: number },
    el: HTMLDivElement
  ) {
    const startLL = L.latLng(a.lat, a.lng);
    const endLL = L.latLng(b.lat, b.lng);

    this.map = L.map(el, { zoomControl: false }).setView(startLL, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    L.control.zoom({ position: 'bottomright' }).addTo(this.map);

    const sidebar = this.instructionsEl?.nativeElement;

    // ✅ OSTAVI show:true da bi uvek odmah računao kao pre
    this.routing = (L as any).Routing.control({
      waypoints: [startLL, endLL],
      routeWhileDragging: false,
      addWaypoints: false,
      draggableWaypoints: false,

      showAlternatives: false,
      fitSelectedRoutes: true,

      show: true,
      collapsible: false,

      createMarker: (_i: number, wp: any) => {
        return L.marker(wp.latLng, { draggable: false });
      },

      router: (L as any).Routing.osrmv1({
        serviceUrl: 'https://router.project-osrm.org/route/v1',
      }),
    }).addTo(this.map);

    // ✅ prebaci routing panel u sidebar (gde pokazuje strelica)
    setTimeout(() => {
      try {
        const panelEl: HTMLElement | undefined = this.routing?._container;
        if (!panelEl || !sidebar) return;

        sidebar.innerHTML = '';
        sidebar.appendChild(panelEl);

        panelEl.style.position = 'static';
        panelEl.style.width = '100%';
      } catch {}
    }, 0);

    // distanca/vreme + cena
    this.routing.on('routesfound', (e: any) => {
      const route = e.routes?.[0];
      if (!route?.summary) return;

      const distM = route.summary.totalDistance ?? 0;
      const timeS = route.summary.totalTime ?? 0;

      const km = Math.max(0, distM / 1000);
      const min = Math.max(0, timeS / 60);

      this.distanceKm = km;
      this.durationMin = min;

      this.routeDistanceText = `${km.toFixed(1)} km`;
      this.routeTimeText = `${Math.round(min)} min`;

      this.ridePrice = this.calculatePrice(km, min);
    });

    setTimeout(() => this.map?.invalidateSize(), 0);
  }

  private calculatePrice(distanceKm: number, durationMin: number) {
    const base = 200;
    const perKm = 80;
    const perMin = 10;
    return Math.round(base + distanceKm * perKm + durationMin * perMin);
  }

  ngOnDestroy(): void {
    this.resetMapState();
  }
}