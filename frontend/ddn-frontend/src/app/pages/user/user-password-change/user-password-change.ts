import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PasswordChangeHttpDataSource } from '../../../api/account/password-change.http-data-source';

@Component({
  selector: 'app-user-password-change',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './user-password-change.html',
  styleUrl: './user-password-change.css',
})
export class UserPasswordChangeComponent {
  private api = inject(PasswordChangeHttpDataSource);

  loading = false;
  successMsg: string | null = null;
  errorMsg: string | null = null;

  form = new FormGroup({
    currentPassword: new FormControl('', [Validators.required]),
    newPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
    confirmNewPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
  });

  onSubmit(): void {
    this.successMsg = null;
    this.errorMsg = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const currentPassword = String(this.form.value.currentPassword ?? '');
    const newPassword = String(this.form.value.newPassword ?? '');
    const confirmNewPassword = String(this.form.value.confirmNewPassword ?? '');

    if (newPassword !== confirmNewPassword) {
      this.errorMsg = 'New password and confirm password do not match.';
      return;
    }

    this.loading = true;

    this.api.changePassword({ currentPassword, newPassword, confirmNewPassword }).subscribe({
      next: () => {
        this.loading = false;
        this.successMsg = 'Password successfully changed.';
        this.form.reset();
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg =
          err?.error?.message ||
          (typeof err?.error === 'string' ? err.error : null) ||
          'Cannot change password.';
      },
    });
  }
}
