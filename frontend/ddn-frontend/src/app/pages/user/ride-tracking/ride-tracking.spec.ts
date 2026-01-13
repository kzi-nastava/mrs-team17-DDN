import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideTrackingComponent } from './ride-tracking';

describe('RideTracking', () => {
  let component: RideTrackingComponent;
  let fixture: ComponentFixture<RideTrackingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideTrackingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideTrackingComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
