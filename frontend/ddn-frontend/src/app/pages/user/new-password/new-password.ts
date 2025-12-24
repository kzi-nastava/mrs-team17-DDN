import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-new-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './new-password.html',
  styleUrls: ['./new-password.css']
})
export class NewPassword {
  //  Reactive forma za novu lozinku
  // - password: obavezno polje, minimum 6 karaktera
  // - confirmPassword: obavezno polje
  newPasswordForm: FormGroup = new FormGroup({
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
    confirmPassword: new FormControl('', Validators.required)
  });

  //  Router za navigaciju posle uspešnog setovanja lozinke
  constructor(private router: Router) {}

  //  Submit handler
  onSubmit(): void {
    //  Provera da li je forma validna
    if (this.newPasswordForm.valid) {
      const { password, confirmPassword } = this.newPasswordForm.value;

      //  Provera da li se lozinke poklapaju
      if (password === confirmPassword) {
        console.log('Password set:', password);

        //  Navigacija na success stranicu
        this.router.navigate(['/success']);
      } else {
        //  Ako lozinke nisu iste
        console.error('Passwords do not match');
      }
    }
  }
}