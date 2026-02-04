import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DriverStateService } from '../../state/driver-state.service';
import { AuthStore } from '../../api/auth/auth.store';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
})
export class NavbarComponent {
  private driverState = inject(DriverStateService);
  private auth = inject(AuthStore);
  private router = inject(Router);

  driverAvailable$ = this.driverState.available$;

  logout(): void {
    this.auth.clear();
    this.driverState.setDriverId(null);
    this.driverState.setAvailable(false);
    this.router.navigate(['/login']);
  }
}
