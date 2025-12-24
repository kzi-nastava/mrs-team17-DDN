import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-public-navbar',
  standalone: true,              
  imports: [RouterModule],       
  templateUrl: './public-navbar.html',
  styleUrls: ['./public-navbar.css'],
})
export class PublicNavbarComponent {}