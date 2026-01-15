import { Routes } from '@angular/router';

import { LandingComponent } from './pages/landing/landing';

import { DriverLayoutComponent } from './pages/driver/driver-layout/driver-layout';
import { DriverHomeComponent } from './pages/driver/driver-home/driver-home';
import { DriverRideHistoryComponent } from './pages/driver/driver-ride-history/driver-ride-history';
import { DriverRideDetailsComponent } from './pages/driver/driver-ride-details/driver-ride-details';
import { DriverPasswordChangeComponent } from './pages/driver/driver-password-change/driver-password-change';
import { DriverProfile } from './pages/driver/driver-profile/driver-profile';
import { DriverActiveRideComponent } from './pages/driver/driver-active-ride/driver-active-ride';

import { LoginComponent } from './pages/user/login/login.component';
import { RideTrackingComponent } from './pages/user/ride-tracking/ride-tracking';

import { AdminLayout } from './pages/admin/admin-layout/admin-layout';
import { AdminHome } from './pages/admin/admin-home/admin-home';
import { AdminUpdateRequests } from './pages/admin/admin-update-requests/admin-update-requests';

import { RIDE_TRACKING_DS } from './pages/user/ride-tracking/ride-tracking.datasource';
import { RideTrackingMockDataSource } from './pages/user/ride-tracking/ride-tracking.mock.datasource';

import { RIDE_LIFECYCLE_DS } from './pages/driver/driver-active-ride/ride-lifecycle.datasource';
import { RideLifecycleMockDataSource } from './pages/driver/driver-active-ride/ride-lifecycle.mock.datasource';

import { AdminCreateDriver } from './pages/admin/admin-create-driver/admin-create-driver';

import { UserLayout } from './pages/user/user-layout/user-layout';
import { UserOrderRide } from './pages/user/user-order-ride/user-order-ride';
import { UserFavouriteRides } from './pages/user/user-favourite-rides/user-favourite-rides';
import { UserFavouriteRideDetails } from './pages/user/user-favourite-ride-details/user-favourite-ride-details';

export const routes: Routes = [
  { path: '', component: LandingComponent },

  {
    path: 'user/ride-tracking',
    component: RideTrackingComponent,
    providers: [
      { provide: RIDE_TRACKING_DS, useClass: RideTrackingMockDataSource },
    ],
  },

  {
  path: 'user',
  component: UserLayout,
  children: [
    { path: 'home', loadComponent: () => import('./pages/user/user-home/user-home').then(m => m.UserHome) },
    { path: 'ride-history', loadComponent: () => import('./pages/user/user-ride-history/user-ride-history').then(m => m.UserRideHistory) },
    { path: 'reports', loadComponent: () => import('./pages/user/user-reports/user-reports').then(m => m.UserReports) },
    { path: 'profile', loadComponent: () => import('./pages/user/user-profile/user-profile').then(m => m.UserProfile) },
    { path: 'ride-tracking', component: RideTrackingComponent,
      providers: [
        { provide: RIDE_TRACKING_DS, useClass: RideTrackingMockDataSource },
      ],
    },
    { path: 'order-ride', component: UserOrderRide },
    { path: 'favourite-rides', component: UserFavouriteRides },
    { path: 'favourite-rides/:id', component: UserFavouriteRideDetails },
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
      {
        path: 'active-ride',
        component: DriverActiveRideComponent,
        providers: [{ provide: RIDE_LIFECYCLE_DS, useClass: RideLifecycleMockDataSource }],},
      { path: 'ride-history', component: DriverRideHistoryComponent },
      { path: 'ride-details/:id', component: DriverRideDetailsComponent },
      { path: 'password-change', component: DriverPasswordChangeComponent },
      { path: 'profile', component: DriverProfile },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },

  { path: 'login', component: LoginComponent },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./pages/user/reset-password/reset-password')
        .then(m => m.ResetPassword)
  },
  {
    path: 'new-password',
    loadComponent: () =>
      import('./pages/user/new-password/new-password')
        .then(m => m.NewPassword)
  },
  {
    path: 'success',
    loadComponent: () =>
      import('./pages/user/success-password/success-password')
        .then(m => m.SuccessPassword)
  },
  {
    path: 'sign-up',
    loadComponent: () =>
      import('./pages/user/sign-up/sign-up')
        .then(m => m.SignUp)
  },
  {
    path: 'sign-up-confirmed',
    loadComponent: () =>
      import('./pages/user/sign-up-confirmed/sign-up-confirmed')
        .then(m => m.SignUpConfirmed)
  },

  {
    path: 'admin',
    component: AdminLayout,
    children: [
      { path: 'home', component: AdminHome },
      { path: 'update-requests', component: AdminUpdateRequests },
      { path: 'create-driver', component: AdminCreateDriver },
      { path: 'admin', redirectTo: 'admin/home', pathMatch: 'full' },
    ],
  },

];
