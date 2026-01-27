import { AfterViewInit, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { FormsModule } from '@angular/forms';

type RideStatus = 'ACTIVE' | 'STOPPED' | 'CANCELLED' | 'PANIC';


@Component({
  selector: 'app-driver-active-ride',
  standalone: true,
  imports: [CommonModule, FormsModule],  
  templateUrl: './active-ride.html',
  styleUrls: ['./active-ride.css']
})
export class DriverActiveRideComponent implements AfterViewInit {
hasActiveRide: any;
stopNote: any;
declineRide() {
throw new Error('Method not implemented.');
}
acceptRide() {
throw new Error('Method not implemented.');
}


status: RideStatus = 'ACTIVE';


// UI state
showCancelModal = false;
showStopModal = false;
showPanicModal = false;
cancelReason = '';


private map!: L.Map;
showNewRideModal: any;


ngAfterViewInit(): void {
this.map = L.map('driverRideMap', {
center: [45.2671, 19.8335],
zoom: 13
});


L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
maxZoom: 19,
attribution: '&copy; OpenStreetMap contributors'
}).addTo(this.map);


setTimeout(() => this.map.invalidateSize(), 0);
}


openCancel() { this.showCancelModal = true; }
closeCancel() { this.showCancelModal = false; }
confirmCancel() {
if (!this.cancelReason.trim()) return;
this.status = 'CANCELLED';
this.showCancelModal = false;
}


openStop() { this.showStopModal = true; }
closeStop() { this.showStopModal = false; }
confirmStop() {
this.status = 'STOPPED';
this.showStopModal = false;
}


openPanic() { this.showPanicModal = true; }
closePanic() { this.showPanicModal = false; }
confirmPanic() {
this.status = 'PANIC';
this.showPanicModal = false;
}
}