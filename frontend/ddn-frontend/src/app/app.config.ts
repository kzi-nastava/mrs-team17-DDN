import { ApplicationConfig, InjectionToken, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';
import { RIDE_LIFECYCLE_DS } from './api/driver/ride-lifecycle.datasource';
import { RideLifecycleHttpDataSource } from './api/driver/ride-lifecycle.http.datasource';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),
    provideZoneChangeDetection(),

    { provide: API_BASE_URL, useValue: 'http://localhost:8080/api' },
    { provide: RIDE_LIFECYCLE_DS, useClass: RideLifecycleHttpDataSource },
  ],
};
