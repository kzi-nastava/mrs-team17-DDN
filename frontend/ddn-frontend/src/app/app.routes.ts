import { Routes } from '@angular/router';

import { LandingComponent } from './pages/landing/landing';

import { DriverLayoutComponent } from './pages/driver/driver-layout/driver-layout';
import { DriverHomeComponent } from './pages/driver/driver-home/driver-home';
import { DriverRideHistoryComponent } from './pages/driver/driver-ride-history/driver-ride-history';
import { DriverRideDetailsComponent } from './pages/driver/driver-ride-details/driver-ride-details';
import { DriverPasswordChangeComponent } from './pages/driver/driver-password-change/driver-password-change';
import { DriverProfile } from './pages/driver/driver-profile/driver-profile';

import { LoginComponent } from './pages/user/login/login.component';
import { RideTrackingComponent } from './pages/user/ride-tracking/ride-tracking';

import { AdminLayout } from './pages/admin/admin-layout/admin-layout';
import { AdminHome } from './pages/admin/admin-home/admin-home';
import { AdminUpdateRequests } from './pages/admin/admin-update-requests/admin-update-requests';

import { RIDE_TRACKING_DS } from './pages/user/ride-tracking/ride-tracking.datasource';
import { RideTrackingMockDataSource } from './pages/user/ride-tracking/ride-tracking.mock.datasource';

export const routes: Routes = [
  { path: '', component: LandingComponent },

  {
    path: 'user/ride-tracking',
    component: RideTrackingComponent,
    providers: [
      { provide: RIDE_TRACKING_DS, useClass: RideTrackingMockDataSource },
    ],
  },

  { path: 'login', component: LoginComponent },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./pages/user/reset-password/reset-password').then(
        m => m.ResetPassword
      ),
  },
  {
    path: 'new-password',
    loadComponent: () =>
      import('./pages/user/new-password/new-password').then(
        m => m.NewPassword
      ),
  },
  {
    path: 'success',
    loadComponent: () =>
      import('./pages/user/success-password/success-password').then(
        m => m.SuccessPassword
      ),
  },
  {
    path: 'sign-up',
    loadComponent: () =>
      import('./pages/user/sign-up/sign-up').then(m => m.SignUp),
  },
  {
    path: 'sign-up-confirmed',
    loadComponent: () =>
      import('./pages/user/sign-up-confirmed/sign-up-confirmed').then(
        m => m.SignUpConfirmed
      ),
  },

  {
    path: 'driver',
    component: DriverLayoutComponent,
    children: [
      { path: 'home', component: DriverHomeComponent },
      { path: 'ride-history', component: DriverRideHistoryComponent },
      { path: 'ride-details/:id', component: DriverRideDetailsComponent },
      { path: 'password-change', component: DriverPasswordChangeComponent },
      { path: 'profile', component: DriverProfile },
    ],
  },

  {
    path: 'admin',
    component: AdminLayout,
    children: [
      { path: 'home', component: AdminHome },
      { path: 'update-requests', component: AdminUpdateRequests },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },

  // { path: '**', redirectTo: '' },
];
