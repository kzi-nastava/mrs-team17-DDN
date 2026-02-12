import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormGroup,
  FormControl,
  Validators,
} from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css'],
})
export class ResetPassword {
  loading = false;
  errorMsg: string | null = null;

  resetForm = new FormGroup({
    email: new FormControl<string>('', [Validators.required, Validators.email]),
  });

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit(): void {
    this.errorMsg = null;

    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    const email = this.resetForm.value.email!.trim();
    if (!email) return;

    this.loading = true;

    this.http
      .post('/api/password-reset/request', null, {
        params: { email },
      })
      .subscribe({
        next: () => {
          this.loading = false;
          // Always show success message (do not reveal if email exists)
          this.router.navigate(['/success'], {
            queryParams: { mode: 'reset-request' },
          });
        },
        error: (err: HttpErrorResponse) => {
          this.loading = false;

          // Optional: still route to success to avoid revealing info
          // But if you want a generic error, keep it minimal
          this.router.navigate(['/success'], {
            queryParams: { mode: 'reset-request' },
          });
        },
      });
  }
}
