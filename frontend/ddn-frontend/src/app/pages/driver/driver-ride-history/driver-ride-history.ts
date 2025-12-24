import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-driver-ride-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-ride-history.html',
  styleUrl: './driver-ride-history.css',
})
export class DriverRideHistoryComponent {
  constructor(private router: Router) {}

  openRideDetails(id: 1) {
    this.router.navigate(['/driver/ride-details/1']);
  }
}
