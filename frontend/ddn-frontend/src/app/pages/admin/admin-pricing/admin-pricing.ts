import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-pricing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-pricing.html',
  styleUrl: './admin-pricing.css',
})
export class AdminPricing {
  standardPrice = 0;
  luxuryPrice = 0;
  vanPrice = 0;

  saving = false;

  save(): void {
    // TODO: connect backend later
  }
}
