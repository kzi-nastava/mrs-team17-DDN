import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <p *ngIf="loading">Confirming your account...</p>
      <p *ngIf="success" class="text-green-600">Account confirmed! Redirecting...</p>
      <p *ngIf="error" class="text-red-600">{{ error }}</p>
    </div>
  `,
})
export class RegistrationConfirm {
  private route = inject(ActivatedRoute);
  private http = inject(HttpClient);
  private router = inject(Router);

  loading = true;
  success = false;
  error: string | null = null;

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading = false;
      this.error = 'Token is missing.';
      return;
    }

    // adjust backend base URL if needed
    this.http.get(`/api/registration/confirm`, { params: { token }, responseType: 'text' })
      .subscribe({
        next: () => {
          this.loading = false;
          this.success = true;
          setTimeout(() => this.router.navigateByUrl('/login'), 800);
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || 'Invalid or expired token.';
        }
      });
  }
}
