import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverHomeComponent } from './driver-home';

describe('DriverHome', () => {
  let component: DriverHomeComponent;
  let fixture: ComponentFixture<DriverHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverHomeComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
