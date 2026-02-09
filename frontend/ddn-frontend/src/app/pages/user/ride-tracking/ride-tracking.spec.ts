import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { RideTrackingComponent } from './ride-tracking';
import { RideTrackingDataSource, RIDE_TRACKING_DS } from '../../../api/user/ride-tracking.datasource';
import { TrackingState } from '../../../api/user/models/ride-tracking.models';

describe('RideTracking', () => {
  let component: any;
  let fixture: ComponentFixture<RideTrackingComponent>;
  let dsMock: {
    watchMyActiveTracking: ReturnType<typeof vi.fn>;
    submitInconsistencyForMyActiveRide: ReturnType<typeof vi.fn>;
    listInconsistenciesForMyActiveRide: ReturnType<typeof vi.fn>;
  };

  const trackingState: TrackingState = {
    car: { lat: 45.26, lng: 19.83 },
    pickup: { lat: 45.25, lng: 19.82 },
    destination: { lat: 45.27, lng: 19.84 },
    route: [
      { lat: 45.25, lng: 19.82 },
      { lat: 45.27, lng: 19.84 },
    ],
    checkpoints: [],
    etaMinutes: 7,
    distanceKm: 3.4,
    status: 'ACTIVE',
  };

  const createComponent = async (rideId?: string) => {
    dsMock = {
      watchMyActiveTracking: vi.fn().mockReturnValue(of(trackingState)),
      submitInconsistencyForMyActiveRide: vi.fn().mockReturnValue(of(void 0)),
      listInconsistenciesForMyActiveRide: vi.fn().mockReturnValue(of([])),
    };

    await TestBed.configureTestingModule({
      imports: [RideTrackingComponent],
      providers: [
        { provide: RIDE_TRACKING_DS, useValue: dsMock as unknown as RideTrackingDataSource },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap(rideId ? { rideId } : {}),
            },
          },
        },
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideTrackingComponent);
    component = fixture.componentInstance as any;

    component.initMap = vi.fn(() => {
      component.map = {
        invalidateSize: vi.fn(),
        remove: vi.fn(),
      };
    });
    component.applyState = vi.fn();

    component.ngAfterViewInit();
    await fixture.whenStable();
  };

  it('should create', async () => {
    await createComponent();

    expect(component).toBeTruthy();
  });

  it('should use rideId from query param when loading tracking and reporting', async () => {
    await createComponent('123');

    expect(dsMock.watchMyActiveTracking).toHaveBeenCalledWith(123);

    component.rideStatus = 'ACTIVE';
    component.reportText = 'Driver route looks inconsistent';
    component.submitReport();

    expect(dsMock.submitInconsistencyForMyActiveRide)
      .toHaveBeenCalledWith('Driver route looks inconsistent', 123);
  });

  it('should fall back to active ride tracking when query param is missing', async () => {
    await createComponent();

    expect(dsMock.watchMyActiveTracking).toHaveBeenCalledWith(undefined);
  });
});
