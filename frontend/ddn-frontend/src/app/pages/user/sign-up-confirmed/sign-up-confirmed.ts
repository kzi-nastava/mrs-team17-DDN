import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sign-up-confirmed',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sign-up-confirmed.html',
  styleUrls: ['./sign-up-confirmed.css']
})
export class SignUpConfirmed {
  
  
  // - navigaciju ka login stranici
  // - timer koji automatski preusmerava korisnika
  // - ili dugme "Go to login"
}