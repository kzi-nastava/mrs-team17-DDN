import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  //  Reactive forma za login
  // - email: obavezno polje + validacija formata
  // - password: obavezno polje
  loginForm: FormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', Validators.required)
  });

  //  Router se koristi za navigaciju posle uspešnog logina
  constructor(private router: Router) {}

  //  Submit handler
  onSubmit(): void {
    //  Provera da li je forma validna
    if (this.loginForm.valid) {
      console.log(this.loginForm.value);

      //  Ovde ide poziv ka AuthService (backend login)
      // this.authService.login(this.loginForm.value).subscribe(...)

      //  Redirect posle uspešnog logina
      this.router.navigate(['/user/home']);
    }
  }
}