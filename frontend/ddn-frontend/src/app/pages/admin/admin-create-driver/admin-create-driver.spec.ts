import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgForm } from '@angular/forms';
import { Observable, of, throwError } from 'rxjs';

import {
  AdminCreateDriverRequest,
  AdminCreateDriverResponse,
  AdminDriverApiService,
} from '../../../api/admin/admin-create-driver.http-data-source';
import { AdminCreateDriver } from './admin-create-driver';

describe('AdminCreateDriver', () => {
  let fixture: ComponentFixture<AdminCreateDriver>;
  let component: AdminCreateDriver;
  let apiStub: {
    calls: AdminCreateDriverRequest[];
    responseFactory: () => Observable<AdminCreateDriverResponse>;
    createDriver: (payload: AdminCreateDriverRequest) => Observable<AdminCreateDriverResponse>;
  };

  const successResponse: AdminCreateDriverResponse = {
    driverId: 42,
    email: 'driver@example.com',
    status: 'CREATED',
    activationLinkValidHours: 24,
  };

  const makeForm = (valid: boolean): NgForm & { resetCalls: unknown[] } => {
    const resetCalls: unknown[] = [];
    return {
      valid,
      resetForm: (value?: unknown) => {
        resetCalls.push(value);
      },
      resetCalls,
    } as unknown as NgForm & { resetCalls: unknown[] };
  };

  beforeEach(async () => {
    apiStub = {
      calls: [],
      responseFactory: () => of(successResponse),
      createDriver: (payload: AdminCreateDriverRequest) => {
        apiStub.calls.push(payload);
        return apiStub.responseFactory();
      },
    };

    await TestBed.configureTestingModule({
      imports: [AdminCreateDriver],
      providers: [
        {
          provide: AdminDriverApiService,
          useValue: apiStub,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminCreateDriver);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should keep selected file when present', () => {
    const file = new File(['photo'], 'driver.png', { type: 'image/png' });
    const event = {
      target: {
        files: [file],
      },
    } as unknown as Event;

    component.onFileSelected(event);

    expect(component.selectedFile).toBe(file);
  });

  it('should block submit for invalid form', () => {
    component.submit(makeForm(false));

    expect(component.errorMsg).toBe('Please fill in all required fields.');
    expect(apiStub.calls.length).toBe(0);
  });

  it('should block submit when seats are out of allowed range', () => {
    const form = makeForm(true);
    component.model.seats = 0;

    component.submit(form);

    expect(component.errorMsg).toBe('Capacity (seats) must be a number between 1 and 9.');
    expect(apiStub.calls.length).toBe(0);
  });

  it('should submit entered data and reset form on success', () => {
    const form = makeForm(true);

    component.model.firstName = 'Ana';
    component.model.lastName = 'Anic';
    component.model.address = 'Bulevar Oslobodjenja 1';
    component.model.phoneNumber = '+381601234567';
    component.model.email = 'driver@example.com';
    component.model.vehicleModel = 'Skoda Octavia';
    component.model.vehicleType = 'LUXURY' as any;
    component.model.licensePlate = 'NS-123-AB';
    component.model.seats = 5 as any;
    component.model.babyTransport = true;
    component.model.petTransport = false;

    component.submit(form);

    expect(apiStub.calls.length).toBe(1);
    expect(apiStub.calls[0]).toEqual({
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
    });

    expect(component.successMsg).toBe(
      'Driver created. Activation email sent to driver@example.com. Link valid: 24h.',
    );
    expect(component.errorMsg).toBe('');
    expect(component.isSubmitting).toBe(false);
    expect(form.resetCalls).toEqual([{
      vehicleType: 'standard',
      seats: 4,
      babyTransport: false,
      petTransport: false,
    }]);
  });

  it('should show backend error message on failed submit', () => {
    apiStub.responseFactory = () =>
      throwError(() => ({ error: { message: 'Driver already exists.' } }));

    component.submit(makeForm(true));

    expect(component.errorMsg).toBe('Driver already exists.');
    expect(component.successMsg).toBe('');
    expect(component.isSubmitting).toBe(false);
  });

  it('should use fallback error message when backend message is missing', () => {
    apiStub.responseFactory = () => throwError(() => ({ error: null }));

    component.submit(makeForm(true));

    expect(component.errorMsg).toBe('Request failed. Please check input and try again.');
    expect(component.isSubmitting).toBe(false);
  });

  it('should ignore submit while request is already in progress', () => {
    component.isSubmitting = true;

    component.submit(makeForm(true));

    expect(apiStub.calls.length).toBe(0);
  });
});
