import { AfterViewInit, Component } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-driver-home',
  standalone: true,
  imports: [],
  templateUrl: './driver-home.html',
  styleUrl: './driver-home.css',
})
export class DriverHomeComponent implements AfterViewInit {
  private map!: L.Map;

  ngAfterViewInit(): void {
    this.map = L.map('startRideMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    const routePoints: L.LatLngExpression[] = [
      [45.2671, 19.8335], 
      [45.2609, 19.8350], 
    ];

    const routeLine = L.polyline(routePoints, {
      weight: 5,
      opacity: 0.9,
      color: '#28c200',
    }).addTo(this.map);

    routePoints.forEach((p, idx) => {
      const isStart = idx === 0;
      const isEnd = idx === routePoints.length - 1;

      L.circleMarker(p, {
        radius: 7,
        color: '#ffffff',
        weight: 2,
        fillColor: '#28c200',
        fillOpacity: 0.95,
      })
        .addTo(this.map)
        .bindPopup(isStart ? 'Start' : isEnd ? 'End' : 'Point');
    });

    this.map.fitBounds(routeLine.getBounds(), { padding: [30, 30] });

    setTimeout(() => this.map.invalidateSize(), 0);
  }
}
