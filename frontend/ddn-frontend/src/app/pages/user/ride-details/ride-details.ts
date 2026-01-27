import { AfterViewInit, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';

type RideStatus = 'ACTIVE' | 'CANCELLED' | 'PANIC';

@Component({
  selector: 'app-ride-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ride-details.html',
  styleUrl: './ride-details.css',
})
export class RideDetailsComponent implements AfterViewInit {
  /**
   * Kasnije (kad budete vezivali):
   * - rideId može doći iz rute
   * - podatke povlačiš iz servisa i setuješ u rideDetails
   */
  @Input() rideId?: number;

  // Podaci koje ćeš popuniti iz baze preko servisa (za sada prazno)
  rideDetails: {
    from?: string;
    to?: string;
    driver?: string;
    vehicle?: string;
    eta?: string;
  } = {};

  status: RideStatus = 'ACTIVE';

  // UI state
  showCancelModal = false;
  showPanicModal = false;

  // Leaflet
  private map!: L.Map;
  private layer = L.layerGroup();
ride: any;

  ngAfterViewInit(): void {
    this.map = L.map('rideMap', {
      center: [45.2671, 19.8335],
      zoom: 13,
      zoomControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors',
    }).addTo(this.map);

    this.layer.addTo(this.map);

    // Placeholder marker da se vidi da mapa radi
    const center: L.LatLngExpression = [45.2671, 19.8335];
    L.circleMarker(center, {
      radius: 7,
      color: '#3b82f6',
      fillColor: '#3b82f6',
      fillOpacity: 0.9,
      weight: 2,
    })
      .addTo(this.layer)
      .bindPopup('Ride location');

    setTimeout(() => this.map.invalidateSize(), 0);

    // Kasnije: ovde pozoveš servis, popuniš rideDetails i iscrtaš rutu
    // this.loadRide(this.rideId)
  }

  // Cancel
  openCancel(): void {
    if (this.status !== 'ACTIVE') return;
    this.showCancelModal = true;
  }
  closeCancel(): void {
    this.showCancelModal = false;
  }
  confirmCancel(): void {
    this.status = 'CANCELLED';
    this.showCancelModal = false;
  }

  // Panic
  openPanic(): void {
    if (this.status !== 'ACTIVE') return;
    this.showPanicModal = true;
  }
  closePanic(): void {
    this.showPanicModal = false;
  }
  confirmPanic(): void {
    this.status = 'PANIC';
    this.showPanicModal = false;
  }
}