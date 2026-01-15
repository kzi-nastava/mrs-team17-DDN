import { CommonModule } from '@angular/common';
import { AfterViewInit, Component } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import * as L from 'leaflet';

type FavouriteRideDetailsVm = {
  id: number;
  start: string;
  checkpoints: string[];
  end: string;
};

@Component({
  selector: 'app-user-favourite-ride-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-favourite-ride-details.html',
  styleUrl: './user-favourite-ride-details.css',
})
export class UserFavouriteRideDetails implements AfterViewInit {
  private map!: L.Map;

  vm: FavouriteRideDetailsVm = {
    id: 0,
    start: 'Futoška 25',
    checkpoints: ['Futoška 35', 'Futoška 45' , 'Futoška 50'],
    end: 'Futoška 55',
  };

  constructor(private route: ActivatedRoute) {
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

    const routePoints: L.LatLngExpression[] = [
      [45.2671, 19.8335],
      [45.2685, 19.8402],
      [45.2657, 19.8460], 
      [45.2628, 19.8425], 
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
    radius: isStart || isEnd ? 7 : 6,
    color: '#ffffff',
    weight: 2,
    fillColor: '#28c200',
    fillOpacity: 0.95,
  })

      .addTo(this.map)
      .bindPopup(
        isStart
          ? 'Start'
          : isEnd
          ? 'End'
          : `Checkpoint ${idx}`
      );
  });

  this.map.fitBounds(routeLine.getBounds(), { padding: [30, 30] });

      setTimeout(() => this.map.invalidateSize(), 0);
    }
  }
