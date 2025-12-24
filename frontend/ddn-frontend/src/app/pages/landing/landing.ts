import { Component } from '@angular/core';
import { PublicNavbarComponent } from '../../components/public-navbar/public-navbar';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [PublicNavbarComponent],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class LandingComponent {}
