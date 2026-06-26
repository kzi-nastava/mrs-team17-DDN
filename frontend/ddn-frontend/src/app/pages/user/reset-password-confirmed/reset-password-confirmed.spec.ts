import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResetPasswordConfirmed } from './reset-password-confirmed';

describe('ResetPasswordConfirmed', () => {
  let component: ResetPasswordConfirmed;
  let fixture: ComponentFixture<ResetPasswordConfirmed>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResetPasswordConfirmed]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ResetPasswordConfirmed);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
