import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../../../components/navbar/navbar';

@Component({
  selector: 'app-driver-layout',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  templateUrl: './driver-layout.html',
})
export class DriverLayoutComponent {}
