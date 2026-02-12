import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormGroup,
  FormControl,
  Validators,
} from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-new-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './new-password.html',
  styleUrls: ['./new-password.css'],
})
export class NewPassword {
  token: string | null = null;

  loading = false;
  serverError: string | null = null;

  newPasswordForm: FormGroup = new FormGroup({
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmPassword: new FormControl('', Validators.required),
  });

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router
  ) {
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  onSubmit(): void {
    this.serverError = null;

    if (!this.token) {
      this.serverError = 'Missing token. Please request a new reset link.';
      return;
    }

    if (this.newPasswordForm.invalid) {
      this.newPasswordForm.markAllAsTouched();
      return;
    }

    const password = String(this.newPasswordForm.value.password ?? '').trim();
    const confirmPassword = String(this.newPasswordForm.value.confirmPassword ?? '').trim();

    if (password !== confirmPassword) {
      this.serverError = 'Passwords do not match.';
      return;
    }

    this.loading = true;

    const body = {
      token: this.token,
      newPassword: password,
      confirmNewPassword: confirmPassword,
    };

    this.http.post('/api/password-reset/confirm', body).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/success']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;

        if (err.status === 400) {
          this.serverError = 'Invalid or expired link. Please request a new reset link.';
        } else if (err.status === 409) {
          this.serverError = 'This link was already used. Please request a new reset link.';
        } else {
          this.serverError = 'Something went wrong. Please try again.';
        }
      },
    });
  }
}
