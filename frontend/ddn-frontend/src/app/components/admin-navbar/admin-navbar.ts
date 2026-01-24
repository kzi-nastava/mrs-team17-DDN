import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthStore } from '../../api/auth/auth.store';

@Component({
  selector: 'app-admin-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-navbar.html',
  styleUrl: './admin-navbar.css',
})
export class AdminNavbar {
  private auth = inject(AuthStore);
  private router = inject(Router);

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
