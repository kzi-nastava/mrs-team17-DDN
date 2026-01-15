import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { UserNavbarComponent } from '../../../components/user-navbar/user-navbar';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [UserNavbarComponent, RouterOutlet],
  templateUrl: './user-layout.html',
  styleUrl: './user-layout.css',
})
export class UserLayout {}
