import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

import { AuthApi } from '../../../api/auth/auth.api';
import { AuthStore } from '../../../api/auth/auth.store';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class LoginComponent {
  private router = inject(Router);
  private authApi = inject(AuthApi);
  private authStore = inject(AuthStore);

  loginForm: FormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required),
  });

  loading = false;
  error: string | null = null;

  onSubmit(): void {
    this.error = null;

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const email = String(this.loginForm.value.email ?? '').trim();
    const password = String(this.loginForm.value.password ?? '');

    this.loading = true;

    this.authApi.login({ email, password }).subscribe({
      next: (resp) => {
        const token = resp?.token;
        if (!token) {
          this.error = 'Login failed (missing token).';
          this.loading = false;
          return;
        }

        this.authStore.setToken(token);

        const role = this.authStore.getRoleFromToken(token);

        if (role === 'DRIVER') {
          this.router.navigate(['/driver/home']);
        } else if (role === 'ADMIN') {
          this.router.navigate(['/admin/home']);
        } else {
          this.router.navigate(['/user/home']);
        }

        this.loading = false;
      },
      error: (err) => {
        // 401 invalid credentials, 403 blocked/inactive (kako si već napravio)
        const status = err?.status;
        if (status === 401) this.error = 'Invalid email or password.';
        else if (status === 403) this.error = 'Account is blocked or inactive.';
        else this.error = 'Login failed. Try again.';

        this.loading = false;
      },
    });
  }
}
