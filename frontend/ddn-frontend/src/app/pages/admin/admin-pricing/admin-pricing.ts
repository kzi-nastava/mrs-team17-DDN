import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminPricingHttpDataSource } from '../../../api/admin/admin-pricing.http.datasource';

@Component({
  selector: 'app-admin-pricing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-pricing.html',
  styleUrl: './admin-pricing.css',
})
export class AdminPricing implements OnInit {
  private api = inject(AdminPricingHttpDataSource);

  standardPrice = 0;
  luxuryPrice = 0;
  vanPrice = 0;

  loading = true;
  saving = false;
  error = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.api.get().subscribe({
      next: (p) => {
        this.standardPrice = p.standard ?? 0;
        this.luxuryPrice = p.luxury ?? 0;
        this.vanPrice = p.van ?? 0;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load pricing';
        this.loading = false;
      },
    });
  }

  save(): void {
    this.saving = true;
    this.error = '';
    this.api.update({
      standard: Number(this.standardPrice),
      luxury: Number(this.luxuryPrice),
      van: Number(this.vanPrice),
    }).subscribe({
      next: () => (this.saving = false),
      error: () => {
        this.error = 'Failed to save pricing';
        this.saving = false;
      },
    });
  }
}
