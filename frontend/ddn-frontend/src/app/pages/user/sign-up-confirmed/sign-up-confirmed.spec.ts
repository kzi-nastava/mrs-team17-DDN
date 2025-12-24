import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignUpConfirmed } from './sign-up-confirmed';

describe('SignUpConfirmed', () => {
  let component: SignUpConfirmed;
  let fixture: ComponentFixture<SignUpConfirmed>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SignUpConfirmed]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SignUpConfirmed);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
