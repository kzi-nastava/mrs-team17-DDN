import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideRouter, RouterLink } from '@angular/router';
import { of } from 'rxjs';

import { UserNavbarComponent } from './user-navbar';
import { NotificationApi } from '../../api/notifications/notification.api';

describe('UserNavbar', () => {
  let component: UserNavbarComponent;
  let fixture: ComponentFixture<UserNavbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserNavbarComponent],
      providers: [
        provideRouter([]),
        {
          provide: NotificationApi,
          useValue: {
            getUnreadCount: () => of(0),
            getMy: () => of([]),
            markRead: () => of(void 0),
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

  it('should point panic and support links to /user/support', () => {
    const links = fixture.debugElement
      .queryAll(By.directive(RouterLink))
      .map((el) => el.injector.get(RouterLink).urlTree?.toString());

    expect(links.filter((path) => path === '/user/support').length).toBe(2);
  });
});
