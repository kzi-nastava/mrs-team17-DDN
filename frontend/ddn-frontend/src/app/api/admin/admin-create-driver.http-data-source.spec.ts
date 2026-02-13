import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { API_BASE_URL } from '../../app.config';
import {
  AdminCreateDriverRequest,
  AdminCreateDriverResponse,
  AdminDriverApiService,
} from './admin-create-driver.http-data-source';

describe('AdminDriverApiService', () => {
  let service: AdminDriverApiService;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://test.local/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminDriverApiService,
        { provide: API_BASE_URL, useValue: baseUrl },
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(AdminDriverApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should send entered driver data to backend create endpoint', () => {
    const payload: AdminCreateDriverRequest = {
      firstName: 'Ana',
      lastName: 'Anic',
      address: 'Bulevar Oslobodjenja 1',
      phoneNumber: '+381601234567',
      email: 'driver@example.com',
      vehicleModel: 'Skoda Octavia',
      vehicleType: 'luxury',
      licensePlate: 'NS-123-AB',
      seats: 5,
      babyTransport: true,
      petTransport: false,
    };

    const expected: AdminCreateDriverResponse = {
      driverId: 42,
      email: 'driver@example.com',
      status: 'CREATED',
      activationLinkValidHours: 24,
    };

    let actual: AdminCreateDriverResponse | undefined;

    service.createDriver(payload).subscribe((value) => {
      actual = value;
    });

    const req = httpMock.expectOne(`${baseUrl}/admin/drivers`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(expected);

    expect(actual).toEqual(expected);
  });
});
