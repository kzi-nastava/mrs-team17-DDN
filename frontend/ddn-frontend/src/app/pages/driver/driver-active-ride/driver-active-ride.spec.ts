import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverActiveRideComponent} from './driver-active-ride';

describe('DriverActiveRide', () => {
  let component: DriverActiveRideComponent;
  let fixture: ComponentFixture<DriverActiveRideComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverActiveRideComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverActiveRideComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
