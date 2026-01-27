import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminDriverApiService, AdminCreateDriverRequest, AdminCreateDriverResponse } from '../../../api/admin/admin-create-driver.http-data-source';

@Component({
  selector: 'app-admin-create-driver',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-create-driver.html',
  styleUrl: './admin-create-driver.css',
})
export class AdminCreateDriver {

  model: AdminCreateDriverRequest = {
    firstName: '',
    lastName: '',
    address: '',
    phoneNumber: '',
    email: '',

    vehicleModel: '',
    vehicleType: 'standard',
    licensePlate: '',
    seats: 4,

    babyTransport: false,
    petTransport: false
  };

  isSubmitting = false;
  successMsg = '';
  errorMsg = '';

  selectedFile?: File;

  constructor(private api: AdminDriverApiService) {}

  onFileSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const f = input.files && input.files.length > 0 ? input.files[0] : undefined;
    this.selectedFile = f;
  }

  submit(form: NgForm): void {
    this.successMsg = '';
    this.errorMsg = '';

    if (this.isSubmitting) return;

    if (!form.valid) {
      this.errorMsg = 'Please fill in all required fields.';
      return;
    }

    const seats = Number(this.model.seats);
    if (!Number.isFinite(seats) || seats < 1 || seats > 9) {
      this.errorMsg = 'Capacity (seats) must be a number between 1 and 9.';
      return;
    }
    this.model.seats = seats;

    this.model.vehicleType = (this.model.vehicleType || 'standard').toLowerCase() as any;

    this.isSubmitting = true;

    this.api.createDriver(this.model).subscribe({
      next: (resp: AdminCreateDriverResponse) => {
        this.successMsg =
          `Driver created. Activation email sent to ${resp.email}. Link valid: ${resp.activationLinkValidHours}h.`;
        form.resetForm({
          vehicleType: 'standard',
          seats: 4,
          babyTransport: false,
          petTransport: false
        });
        this.selectedFile = undefined;
        this.isSubmitting = false;
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;

        const msg =
          err?.error?.message ||
          (typeof err?.error === 'string' ? err.error : '') ||
          'Request failed. Please check input and try again.';

        this.errorMsg = msg;
      }
    });
  }
}
