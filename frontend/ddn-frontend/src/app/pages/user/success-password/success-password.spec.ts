import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SuccessPassword } from './success-password';

describe('SuccessPassword', () => {
  let component: SuccessPassword;
  let fixture: ComponentFixture<SuccessPassword>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SuccessPassword]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SuccessPassword);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
