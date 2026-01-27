import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../api/auth/auth.store';

@Component({
  selector: 'app-user-navbar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './user-navbar.html',
  styleUrl: './user-navbar.css',
})
export class UserNavbarComponent {
  private auth = inject(AuthStore);
  private router = inject(Router);

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
