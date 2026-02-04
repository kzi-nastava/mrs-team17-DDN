import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-ride-status',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-ride-status.html',
  styleUrl: './admin-ride-status.css',
})
export class AdminRideStatus {
  query = '';
  loading = false;

  reload(): void {
    // TODO: connect backend later
    // for now: keep empty state
  }
}
