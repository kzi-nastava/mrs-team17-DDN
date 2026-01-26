import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UserRegistrationApi } from '../../../api/user/user-registration';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sign-up.html',
  styleUrls: ['./sign-up.css']
})
export class SignUp {

  signUpForm: FormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    confirmPassword: new FormControl('', Validators.required),
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    address: new FormControl('', Validators.required),
    number: new FormControl('', Validators.required) // telefon
  });

  errorMessage: string | null = null;

  constructor(
    private router: Router,
    private userRegistrationApi: UserRegistrationApi
  ) { }

  onSubmit(): void {
    if (!this.signUpForm.valid) return;

    const { password, confirmPassword } = this.signUpForm.value;

    if (password !== confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return;
    }

    const payload = {
      email: this.signUpForm.value.email!,
      password: this.signUpForm.value.password!,
      confirmPassword: this.signUpForm.value.confirmPassword!,
      firstName: this.signUpForm.value.name!,
      lastName: this.signUpForm.value.surname!,
      address: this.signUpForm.value.address!,
      phone: this.signUpForm.value.number!
    };

    this.userRegistrationApi.register(payload).subscribe({
      next: (res) => {
        console.log('REGISTER OK', res);
        this.router.navigate(['/sign-up-confirmed']);
      },
      error: (err) => {
        console.error('REGISTER FAIL', err);
        this.errorMessage = err?.error?.message || 'Registration failed';
      }
    });
  }
}