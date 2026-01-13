import { AfterViewInit, Component } from '@angular/core';
import * as L from 'leaflet';
import { PublicNavbarComponent } from '../../components/public-navbar/public-navbar';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [PublicNavbarComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent implements AfterViewInit {
  private map!: L.Map;

  private vehicles = [
    { id: 1, lat: 45.2675, lng: 19.8339, status: 'free' as const },
    { id: 2, lat: 45.2661, lng: 19.8412, status: 'busy' as const },
    { id: 3, lat: 45.2619, lng: 19.8294, status: 'free' as const },
    { id: 4, lat: 45.2703, lng: 19.8258, status: 'busy' as const },
    { id: 5, lat: 45.2731, lng: 19.8386, status: 'free' as const },
    { id: 6, lat: 45.2587, lng: 19.8421, status: 'busy' as const },
    { id: 7, lat: 45.2648, lng: 19.8237, status: 'free' as const },
    { id: 8, lat: 45.2692, lng: 19.8464, status: 'busy' as const },
  ];

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

    this.vehicles.forEach(v => {
      const color = v.status === 'free' ? '#2ecc71' : '#e74c3c';

      const marker = L.circleMarker([v.lat, v.lng], {
        radius: 8,
        color,
        fillColor: color,
        fillOpacity: 0.9,
        weight: 2,
      }).addTo(this.map);

      marker.bindPopup(`Vehicle ${v.id} — ${v.status.toUpperCase()}`);

      marker.on('click', () => {
        this.map.setView([v.lat, v.lng], 15, { animate: true });
      });
    });

    setTimeout(() => this.map.invalidateSize(), 0);
  }
}
