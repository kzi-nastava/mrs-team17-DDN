import { AfterViewInit, ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { PublicNavbarComponent } from '../../components/public-navbar/public-navbar';
import { ActiveVehiclesHttpDataSource } from './active-vehicles.http.datasource';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';


type UiVehicle = { id: number; lat: number; lng: number; status: 'free' | 'busy' };


@Component({
selector: 'app-landing',
standalone: true,
imports: [CommonModule, PublicNavbarComponent, FormsModule],
templateUrl: './landing.html',
styleUrl: './landing.css',
})
export class LandingComponent implements AfterViewInit {
private cdr = inject(ChangeDetectorRef);
private router = inject(Router);


private map!: L.Map;
private ds = inject(ActiveVehiclesHttpDataSource);
private markersLayer = L.layerGroup();


vehicles: UiVehicle[] = [];


// OVO više ne mora da se koristi za "New route",
// ali ostavljamo da ne pravi git haos dok ne očistiš HTML.
showEstimateModal = false;
pickup = '';
destination = '';
estimatedTime: string | null = null;


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


this.loadVehicles();


setTimeout(() => this.map.invalidateSize(), 0);
}


private loadVehicles(): void {
this.ds.getActiveVehicles().subscribe({
next: (list) => {
this.vehicles = list.map(v => ({
id: v.id,
lat: v.latitude,
lng: v.longitude,
status: v.busy ? 'busy' : 'free',
}));


this.renderMarkers();
this.cdr.detectChanges();
},
error: (err) => {
console.error('Failed to load active vehicles', err);
},
});
}


private renderMarkers(): void {
this.markersLayer.clearLayers();


this.vehicles.forEach(v => {
const color = v.status === 'free' ? '#2ecc71' : '#e74c3c';


const marker = L.circleMarker([v.lat, v.lng], {
radius: 8,
color,
fillColor: color,
fillOpacity: 0.9,
weight: 2,
}).addTo(this.markersLayer);


marker.bindPopup(`Vehicle ${v.id} — ${v.status.toUpperCase()}`);


marker.on('click', () => {
this.map.setView([v.lat, v.lng], 15, { animate: true });
});
});
}


// 🔥 NOVO: umesto modala, vodi na Create Route stranicu
openEstimateModal(): void {
this.router.navigate(['/create-route']);
}


// Ostavljamo da ne pravi probleme ako još negde postoje pozivi
closeEstimateModal(): void {
this.showEstimateModal = false;
}


submitEstimate(): void {
this.estimatedTime = '12 minutes';
this.showEstimateModal = false;
}
}