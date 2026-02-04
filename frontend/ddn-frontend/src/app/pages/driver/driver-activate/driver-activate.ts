import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { DriverActivationApi } from '../../../api/driver/driver-activation.http-data-source';

@Component({
  selector: 'app-driver-activate',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './driver-activate.html',
  styleUrls: ['./driver-activate.css'],
})
export class DriverActivate {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(DriverActivationApi);

  token: string | null = null;

  form: FormGroup = new FormGroup({
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
  });

  loading = false;
  success: string | null = null;
  error: string | null = null;

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token || !this.token.trim()) {
      this.error =
        'Invalid link (missing token). Please contact the administrator and request a new activation link.';
    }
  }

  onSubmit(): void {
    this.error = null;
    this.success = null;

    if (!this.token || !this.token.trim()) {
      this.error = 'Invalid link (missing token).';
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const password = String(this.form.value.password ?? '');
    const confirmPassword = String(this.form.value.confirmPassword ?? '');

    if (password !== confirmPassword) {
      this.error = 'Passwords do not match.';
      return;
    }

    this.loading = true;

    this.api
      .activate({ token: this.token.trim(), password, confirmPassword })
      .subscribe({
        next: () => {
          this.loading = false;
          this.success =
            'Your account has been activated successfully. Redirecting you to the login page...';
          setTimeout(() => this.router.navigate(['/login']), 1500);
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;
          const msg =
            (err?.error && (err.error as any)?.message) ||
            (typeof err?.error === 'string' ? err.error : null) ||
            'Activation failed. Please try again or request a new activation link.';
          this.error = msg;
        },
      });
  }
}
