import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrationConfirm } from './registration-confirm';

describe('RegistrationConfirm', () => {
  let component: RegistrationConfirm;
  let fixture: ComponentFixture<RegistrationConfirm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrationConfirm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistrationConfirm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
