import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css']
})
export class ResetPassword {
  //  Reactive forma za reset lozinke
  // - email: obavezno polje + validacija formata
  resetForm: FormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });
  //  Router za navigaciju posle uspešnog submit-a
  constructor(private router: Router) {}
  //  Submit handler
  onSubmit(): void {
    //  Provera da li je forma validna
    if (this.resetForm.valid) {
      console.log('Reset link sent to:', this.resetForm.value.email);

      //  Ovde bi išao poziv ka backend servisu za slanje reset linka
      // this.authService.sendResetLink(this.resetForm.value.email).subscribe(...)

      //  Navigacija na new-password stranicu
      this.router.navigate(['/new-password']);
    }
  }
}