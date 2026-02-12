import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { UserNavbarComponent } from './user-navbar';
import { NotificationApi } from '../../api/notifications/notification.api';
import { Notification } from '../../api/notifications/models/notification.model';

describe('UserNavbar', () => {
  let component: UserNavbarComponent;
  let fixture: ComponentFixture<UserNavbarComponent>;
  let getUnreadCountMock: ReturnType<typeof vi.fn>;
  let getMyMock: ReturnType<typeof vi.fn>;
  let markReadMock: ReturnType<typeof vi.fn>;

  const notifications: Notification[] = [
    {
      id: 1,
      type: 'RIDE_FINISHED',
      title: 'Ride finished',
      message: 'Your ride is finished.',
      linkUrl: '/user/ride-tracking?rideId=10',
      createdAt: '2026-02-09T10:00:00Z',
      readAt: null,
    },
    {
      id: 2,
      type: 'RIDE_ACCEPTED',
      title: 'Ride accepted',
      message: 'Driver accepted your ride.',
      linkUrl: '/user/ride-tracking?rideId=11',
      createdAt: '2026-02-09T09:59:00Z',
      readAt: null,
    },
    {
      id: 3,
      type: 'SCHEDULED_RIDE_REMINDER',
      title: 'Reminder',
      message: 'Your ride starts soon.',
      linkUrl: '/user/ride-tracking?rideId=12',
      createdAt: '2026-02-09T09:58:00Z',
      readAt: null,
    },
    {
      id: 4,
      type: 'RIDE_FINISHED',
      title: 'Older notification',
      message: 'Older ride.',
      linkUrl: '/user/ride-tracking?rideId=13',
      createdAt: '2026-02-09T09:57:00Z',
      readAt: null,
    },
  ];

  beforeEach(async () => {
    getUnreadCountMock = vi.fn().mockReturnValue(of(2));
    getMyMock = vi.fn().mockReturnValue(of(notifications));
    markReadMock = vi.fn().mockReturnValue(of(void 0));

    await TestBed.configureTestingModule({
      imports: [UserNavbarComponent],
      providers: [
        provideRouter([]),
        {
          provide: NotificationApi,
          useValue: {
            getUnreadCount: getUnreadCountMock,
            getMy: getMyMock,
            markRead: markReadMock,
          },
        },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserNavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load only last 3 notifications', () => {
    expect(getMyMock).toHaveBeenCalledWith(3);
    expect(component.notifications.length).toBe(3);
    expect(component.notifications.map((n) => n.id)).toEqual([1, 2, 3]);
  });

  it('should reduce unread count when opening an unread notification', () => {
    component.unreadCount = 2;
    component.notifications = notifications.slice(0, 3).map((n) => ({ ...n }));

    component.clickNotification(component.notifications[0]);

    expect(markReadMock).toHaveBeenCalledWith(1);
    expect(component.unreadCount).toBe(1);
    expect(component.notifications[0].readAt).not.toBeNull();
  });

  it('should point panic and support links to /user/support', () => {
    const links = fixture.debugElement
      .queryAll(By.directive(RouterLink))
      .map((el) => el.injector.get(RouterLink).urlTree?.toString());

    expect(links.filter((path) => path === '/user/support').length).toBe(2);
  });
});
