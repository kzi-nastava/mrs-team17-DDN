import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRideHistoryComponent } from './driver-ride-history';

describe('DriverRideHistory', () => {
  let component: DriverRideHistoryComponent;
  let fixture: ComponentFixture<DriverRideHistoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRideHistoryComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRideHistoryComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
