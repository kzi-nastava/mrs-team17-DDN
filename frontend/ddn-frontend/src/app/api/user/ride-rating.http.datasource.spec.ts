import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { API_BASE_URL } from '../../app.config';
import { RideRatingResponse } from './models/ride-rating.models';
import { RideRatingHttpDataSource } from './ride-rating.http.datasource';

describe('RideRatingHttpDataSource', () => {
  let ds: RideRatingHttpDataSource;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://test.local/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        RideRatingHttpDataSource,
        { provide: API_BASE_URL, useValue: baseUrl },
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    ds = TestBed.inject(RideRatingHttpDataSource);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should request existing rating by ride id', () => {
    const expected: RideRatingResponse = {
      rideId: 15,
      driverRating: 5,
      vehicleRating: 4,
      comment: 'Good',
      createdAt: '2026-02-08T14:00:00Z',
    };
    let actual: RideRatingResponse | null | undefined;

    ds.getRating(15).subscribe(value => {
      actual = value;
    });

    const req = httpMock.expectOne(`${baseUrl}/rides/15/rating`);
    expect(req.request.method).toBe('GET');
    req.flush(expected);

    expect(actual).toEqual(expected);
  });

  it('should map getRating errors to null', () => {
    let actual: RideRatingResponse | null | undefined;

    ds.getRating(44).subscribe(value => {
      actual = value;
    });

    const req = httpMock.expectOne(`${baseUrl}/rides/44/rating`);
    expect(req.request.method).toBe('GET');
    req.flush({ message: 'Not found' }, { status: 404, statusText: 'Not Found' });

    expect(actual).toBeNull();
  });

  it('should submit rating payload to backend endpoint', () => {
    const body = {
      driverRating: 2,
      vehicleRating: 3,
      comment: 'Average ride',
    };
    let completed = false;

    ds.submitRating(77, body).subscribe(() => {
      completed = true;
    });

    const req = httpMock.expectOne(`${baseUrl}/rides/77/rating`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});

    expect(completed).toBe(true);
  });
});
