import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserNavbarComponent } from './user-navbar';

describe('UserNavbar', () => {
  let component: UserNavbarComponent;
  let fixture: ComponentFixture<UserNavbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserNavbarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserNavbarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
