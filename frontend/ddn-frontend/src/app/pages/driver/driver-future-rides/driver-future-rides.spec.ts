import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { DriverFutureRidesComponent } from './driver-future-rides';
import { API_BASE_URL } from '../../../app.config';
import { DriverRidesHttpDataSource } from '../../../api/driver/driver-rides-http.datasource';
import { DriverStateService } from '../../../state/driver-state.service';

describe('DriverFutureRides', () => {
  let component: DriverFutureRidesComponent;
  let fixture: ComponentFixture<DriverFutureRidesComponent>;
  let ridesApiMock: {
    getActiveRide: ReturnType<typeof vi.fn>;
    getUpcomingRides: ReturnType<typeof vi.fn>;
    startRide: ReturnType<typeof vi.fn>;
  };
  let httpMock: { get: ReturnType<typeof vi.fn> };
  let driverStateMock: { setAvailable: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  const acceptedRides = [
    {
      rideId: 101,
      startedAt: null,
      endedAt: null,
      startAddress: 'Start A',
      destinationAddress: 'Destination A',
      stops: [],
      canceled: false,
      canceledBy: null,
      status: 'ACCEPTED',
      price: 1000,
      panicTriggered: false,
      passengers: [],
      reports: [],
    },
    {
      rideId: 102,
      startedAt: null,
      endedAt: null,
      startAddress: 'Start B',
      destinationAddress: 'Destination B',
      stops: [],
      canceled: false,
      canceledBy: null,
      status: 'ACCEPTED',
      price: 1200,
      panicTriggered: false,
      passengers: [],
      reports: [],
    },
  ];

  beforeEach(async () => {
    ridesApiMock = {
      getActiveRide: vi.fn().mockReturnValue(throwError(() => ({ status: 404 }))),
      getUpcomingRides: vi.fn().mockReturnValue(of(acceptedRides)),
      startRide: vi.fn().mockReturnValue(of(void 0)),
    };
    httpMock = {
      get: vi.fn().mockReturnValue(
        of({
          car: { lat: 45.26, lng: 19.83 },
          pickup: { lat: 45.25, lng: 19.82 },
          destination: { lat: 45.27, lng: 19.84 },
          route: [],
          etaMinutes: 9,
          distanceKm: 3,
          status: 'ACCEPTED',
        })
      ),
    };
    driverStateMock = {
      setAvailable: vi.fn(),
    };
    routerMock = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    await TestBed.configureTestingModule({
      imports: [DriverFutureRidesComponent],
      providers: [
        { provide: DriverRidesHttpDataSource, useValue: ridesApiMock },
        { provide: HttpClient, useValue: httpMock },
        { provide: DriverStateService, useValue: driverStateMock },
        { provide: Router, useValue: routerMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { queryParamMap: convertToParamMap({ rideId: '102' }) },
          },
        },
        { provide: API_BASE_URL, useValue: 'http://api' },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DriverFutureRidesComponent);
    component = fixture.componentInstance;

    (component as any).initMap = vi.fn(() => {
      (component as any).map = {
        remove: vi.fn(),
        fitBounds: vi.fn(),
        invalidateSize: vi.fn(),
        removeLayer: vi.fn(),
      };
    });
    (component as any).drawRouteOnMap = vi.fn();

    component.ngAfterViewInit();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load accepted rides and prefer ride from query param', () => {
    expect(component.acceptedRides.map((ride) => ride.rideId)).toEqual([101, 102]);
    expect(component.ride?.rideId).toBe(102);
    expect(httpMock.get).toHaveBeenCalledWith('http://api/rides/102/tracking');
  });
});
