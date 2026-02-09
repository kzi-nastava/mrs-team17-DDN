import { Component, inject, OnDestroy, OnInit } from '@angular/core';
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
export class UserNavbarComponent implements OnInit, OnDestroy {
  private auth = inject(AuthStore);
  private router = inject(Router);
  private notificationsApi = inject(NotificationApi);
  private readonly pollMs = 5000;
  private readonly notificationsLimit = 3;
  private pollTimer: any = null;

  notifications: Notification[] = [];
  unreadCount = 0;
  open = false;

  ngOnInit(): void {
    this.refreshNotifications();
    this.startPolling();
  }

  ngOnDestroy(): void {
    if (this.pollTimer != null) {
      clearInterval(this.pollTimer);
      this.pollTimer = null;
    }
  }

  private refreshNotifications(): void {
    this.notificationsApi.getUnreadCount()
      .subscribe(c => this.unreadCount = c);

    this.notificationsApi.getMy(this.notificationsLimit)
      .subscribe(n => this.notifications = (n ?? []).slice(0, this.notificationsLimit));
  }

  private startPolling(): void {
    if (this.pollTimer != null) return;
    this.pollTimer = setInterval(() => this.refreshNotifications(), this.pollMs);
  }

  toggle(): void {
    this.open = !this.open;
    if (this.open) this.refreshNotifications();
  }

  clickNotification(n: Notification): void {
    if (!n.readAt) {
      this.markReadLocally(n.id);
      this.notificationsApi.markRead(n.id).subscribe({
        error: () => this.refreshNotifications(),
      });
    }
    this.open = false;
    this.router.navigateByUrl(n.linkUrl);
  }

  private markReadLocally(notificationId: number): void {
    let changed = false;
    this.notifications = this.notifications.map((notification) => {
      if (notification.id !== notificationId || notification.readAt) return notification;
      changed = true;
      return { ...notification, readAt: new Date().toISOString() };
    });

    if (changed) {
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    }
  }

  logout(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
