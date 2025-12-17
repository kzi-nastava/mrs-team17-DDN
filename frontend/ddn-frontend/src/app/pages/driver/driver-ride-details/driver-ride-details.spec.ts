import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRideDetailsComponent } from './driver-ride-details';

describe('DriverRideDetails', () => {
  let component: DriverRideDetailsComponent;
  let fixture: ComponentFixture<DriverRideDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRideDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRideDetailsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
