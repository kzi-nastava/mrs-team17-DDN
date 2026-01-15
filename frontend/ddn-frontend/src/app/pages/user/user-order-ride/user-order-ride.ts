import { AfterViewInit, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';

@Component({
  selector: 'app-user-order-ride',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-order-ride.html',
  styleUrl: './user-order-ride.css',
})
export class UserOrderRide implements AfterViewInit {
  private map!: L.Map;

  orderType: 'now' | 'schedule' = 'now';

  checkpointInput = '';
  checkpoints: string[] = [];

  userEmailInput = '';
  linkedUsers: string[] = [];
  private guestCounter = 1;

  addCheckpoint(): void {
    const v = (this.checkpointInput || '').trim();
    if (!v) return;

    this.checkpoints.push(v);
    this.checkpointInput = '';
  }

  addUser(): void {
    const email = (this.userEmailInput || '').trim();

    if (email.length === 0) {
      this.linkedUsers.push(`Guest ${this.guestCounter++}`);
      return;
    }

    this.linkedUsers.push(email);
    this.userEmailInput = '';
  }

  ngAfterViewInit(): void {
    this.map = L.map('orderMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    L.circleMarker([45.2671, 19.8335], {
      radius: 8,
      color: '#2ecc71',
      fillColor: '#2ecc71',
      fillOpacity: 0.9,
      weight: 2,
    }).addTo(this.map);

    setTimeout(() => this.map.invalidateSize(), 0);
  }
}
