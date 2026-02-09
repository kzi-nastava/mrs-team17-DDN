import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { DriverActiveRideComponent } from './driver-active-ride';
import { RIDE_LIFECYCLE_DS } from '../../../api/driver/ride-lifecycle.datasource';
import { DriverRidesHttpDataSource } from '../../../api/driver/driver-rides-http.datasource';
import { DriverStateService } from '../../../state/driver-state.service';

describe('DriverActiveRide', () => {
  let component: DriverActiveRideComponent;
  let fixture: ComponentFixture<DriverActiveRideComponent>;
  let lifecycleMock: { finishRide: ReturnType<typeof vi.fn> };
  let ridesApiMock: {
    getActiveRide: ReturnType<typeof vi.fn>;
    getAcceptedRides: ReturnType<typeof vi.fn>;
  };
  let driverStateMock: { setAvailable: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  const activeRide = {
    rideId: 77,
    startedAt: '2026-02-09T20:00:00Z',
    endedAt: null,
    startAddress: 'Start',
    destinationAddress: 'Destination',
    stops: [],
    canceled: false,
    canceledBy: null,
    status: 'ACTIVE',
    price: 1500,
    panicTriggered: false,
    passengers: [],
    reports: [],
  };

  beforeEach(async () => {
    lifecycleMock = {
      finishRide: vi.fn().mockReturnValue(of(void 0)),
    };
    ridesApiMock = {
      getActiveRide: vi.fn().mockReturnValue(of(activeRide)),
      getAcceptedRides: vi.fn().mockReturnValue(
        of([
          {
            ...activeRide,
            rideId: 88,
            status: 'ACCEPTED',
            startedAt: null,
          },
        ])
      ),
    };
    driverStateMock = {
      setAvailable: vi.fn(),
    };
    routerMock = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    await TestBed.configureTestingModule({
      imports: [DriverActiveRideComponent],
      providers: [
        { provide: RIDE_LIFECYCLE_DS, useValue: lifecycleMock },
        { provide: DriverRidesHttpDataSource, useValue: ridesApiMock },
        { provide: DriverStateService, useValue: driverStateMock },
        { provide: Router, useValue: routerMock },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverActiveRideComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should prepare future rides navigation after finish', () => {
    component.finishRide();

    expect(lifecycleMock.finishRide).toHaveBeenCalledWith(77);
    expect(component.finished).toBe(true);
    expect(component.postFinishTarget).toBe('future');
    expect(component.postFinishRideId).toBe(88);
    expect(routerMock.navigate).not.toHaveBeenCalled();

    component.continueAfterFinish();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/driver/future-rides'], {
      queryParams: { rideId: 88 },
    });
  });

  it('should prepare home navigation when there are no future rides', () => {
    ridesApiMock.getAcceptedRides.mockReturnValueOnce(of([]));

    component.finishRide();

    expect(component.finished).toBe(true);
    expect(component.postFinishTarget).toBe('home');
    expect(component.postFinishRideId).toBeNull();
    expect(driverStateMock.setAvailable).toHaveBeenCalledWith(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();

    component.continueAfterFinish();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/driver/home']);
  });
});
