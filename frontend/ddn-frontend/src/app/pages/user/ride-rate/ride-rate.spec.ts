import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { RideRatingDataSource, RIDE_RATING_DS } from '../../../api/user/ride-rating.datasource';
import { RideRatingResponse } from '../../../api/user/models/ride-rating.models';
import { RideRateComponent } from './ride-rate';

type SetupOptions = {
  rideId?: string;
  getRating$?: Observable<RideRatingResponse | null>;
  submit$?: Observable<void>;
};

describe('RideRateComponent', () => {
  let fixture: ComponentFixture<RideRateComponent>;
  let component: RideRateComponent;
  let dsMock: {
    getRating: ReturnType<typeof vi.fn>;
    submitRating: ReturnType<typeof vi.fn>;
  };
  let navigateMock: ReturnType<typeof vi.fn>;

  const ratingResponse: RideRatingResponse = {
    rideId: 12,
    driverRating: 4,
    vehicleRating: 5,
    comment: 'Very smooth ride',
    createdAt: '2026-02-08T12:00:00Z',
  };

  const buildRoute = (rideId: string | undefined) => {
    const params = rideId === undefined ? {} : { rideId };
    return {
      snapshot: { paramMap: convertToParamMap(params) },
    };
  };

  const createComponent = async (options: SetupOptions = {}) => {
    dsMock = {
      getRating: vi.fn().mockReturnValue(options.getRating$ ?? of(null)),
      submitRating: vi.fn().mockReturnValue(options.submit$ ?? of(void 0)),
    };
    navigateMock = vi.fn();

    await TestBed.configureTestingModule({
      imports: [RideRateComponent],
      providers: [
        { provide: RIDE_RATING_DS, useValue: dsMock as unknown as RideRatingDataSource },
        { provide: ActivatedRoute, useValue: buildRoute(options.rideId ?? '12') },
        { provide: Router, useValue: { navigate: navigateMock } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RideRateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  };

  it('should load existing rating on init', async () => {
    await createComponent({ getRating$: of(ratingResponse) });

    expect(dsMock.getRating).toHaveBeenCalledWith(12);
    expect(component.existing).toEqual(ratingResponse);
    expect(component.info).toBe('You already rated this ride.');
  });

  it('should set error and skip loading when ride id is invalid', async () => {
    await createComponent({ rideId: '0' });

    expect(component.error).toBe('Invalid ride');
    expect(dsMock.getRating).not.toHaveBeenCalled();
  });

  it('should show error when rating fetch fails', async () => {
    await createComponent({ getRating$: throwError(() => new Error('boom')) });

    expect(component.error).toBe('Rating not available');
    expect(component.info).toBe('');
  });

  it('should submit valid ratings and trimmed comment', async () => {
    await createComponent({ getRating$: of(null), submit$: of(void 0) });

    component.driverRating = 1;
    component.vehicleRating = 5;
    component.comment = '  Great driver and clean vehicle  ';

    component.submit();

    expect(dsMock.submitRating).toHaveBeenCalledWith(12, {
      driverRating: 1,
      vehicleRating: 5,
      comment: 'Great driver and clean vehicle',
    });
    expect(component.submitting).toBe(false);
    expect(dsMock.getRating).toHaveBeenCalledTimes(2);
  });

  it('should submit undefined comment when textarea is blank', async () => {
    await createComponent({ getRating$: of(null), submit$: of(void 0) });

    component.driverRating = 5;
    component.vehicleRating = 4;
    component.comment = '   ';
    component.submit();

    expect(dsMock.submitRating).toHaveBeenCalledWith(12, {
      driverRating: 5,
      vehicleRating: 4,
      comment: undefined,
    });
  });

  it('should block submit when rating value is out of range', async () => {
    await createComponent({ getRating$: of(null), submit$: of(void 0) });

    component.driverRating = 0;
    component.vehicleRating = 5;
    component.submit();

    component.driverRating = 5;
    component.vehicleRating = 6;
    component.submit();

    expect(dsMock.submitRating).not.toHaveBeenCalled();
  });

  it('should block submit when rating already exists', async () => {
    await createComponent({ getRating$: of(ratingResponse), submit$: of(void 0) });

    component.submit();

    expect(dsMock.submitRating).not.toHaveBeenCalled();
  });

  it('should set submit error when submit request fails', async () => {
    await createComponent({ getRating$: of(null), submit$: throwError(() => new Error('fail')) });

    component.driverRating = 3;
    component.vehicleRating = 3;
    component.submit();

    expect(component.error).toBe('Submit failed');
    expect(component.submitting).toBe(false);
    expect(dsMock.getRating).toHaveBeenCalledTimes(1);
  });

  it('should navigate back to user root', async () => {
    await createComponent();

    component.goBack();

    expect(navigateMock).toHaveBeenCalledWith(['/user']);
  });
});
