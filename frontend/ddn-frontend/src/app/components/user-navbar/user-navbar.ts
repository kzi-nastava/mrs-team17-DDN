import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthStore } from '../../api/auth/auth.store';
import { NotificationApi } from '../../api/notifications/notification.api';
import { Notification } from '../../api/notifications/models/notification.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './user-navbar.html',
  styleUrl: './user-navbar.css',
})
export class UserNavbarComponent implements OnInit {
  private auth = inject(AuthStore);
  private router = inject(Router);
  private notificationsApi = inject(NotificationApi);

  notifications: Notification[] = [];
  unreadCount = 0;
  open = false;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.notificationsApi.getUnreadCount()
      .subscribe(c => this.unreadCount = c);

    this.notificationsApi.getMy()
      .subscribe(n => this.notifications = n);
  }

  toggle(): void {
    this.open = !this.open;
  }

  clickNotification(n: Notification): void {
    if (!n.readAt) {
      this.notificationsApi.markRead(n.id).subscribe();
    }
    this.open = false;
    this.router.navigateByUrl(n.linkUrl);
  }

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
