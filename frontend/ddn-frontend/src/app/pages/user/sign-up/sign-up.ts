import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sign-up.html',
  styleUrls: ['./sign-up.css']
})
export class SignUp {
  // Reactive forma za registraciju
  
  signUpForm: FormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    confirmPassword: new FormControl('', Validators.required),
    name: new FormControl('', Validators.required),
    surname: new FormControl('', Validators.required),
    address: new FormControl('', Validators.required),
    number: new FormControl('', Validators.required)
  });

  //  Router za navigaciju posle uspešne registracije
  constructor(private router: Router) {}

  //  Submit handler
  onSubmit(): void {
    //  Provera da li je forma validna
    if (this.signUpForm.valid) {
      const { password, confirmPassword } = this.signUpForm.value;

      //  Provera da li se lozinke poklapaju
      if (password === confirmPassword) {
        console.log('Sign-up data:', this.signUpForm.value);

        //  Ovde ide poziv ka backend servisu za registraciju

        
        //  Navigacija na sign-up-confirmed stranicu
        this.router.navigate(['/sign-up-confirmed']);
      } else {
        //  Ako lozinke nisu iste
        console.error('Passwords do not match');
      }
    }
  }
}