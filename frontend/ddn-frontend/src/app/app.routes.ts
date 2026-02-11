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
import { RideRateComponent } from './pages/user/ride-rate/ride-rate';

import { AdminLayout } from './pages/admin/admin-layout/admin-layout';
import { AdminHome } from './pages/admin/admin-home/admin-home';
import { AdminUpdateRequests } from './pages/admin/admin-update-requests/admin-update-requests';
import { AdminCreateDriver } from './pages/admin/admin-create-driver/admin-create-driver';

import { UserLayout } from './pages/user/user-layout/user-layout';
import { UserOrderRide } from './pages/user/user-order-ride/user-order-ride';
import { UserFavouriteRides } from './pages/user/user-favourite-rides/user-favourite-rides';
import { UserFavouriteRideDetails } from './pages/user/user-favourite-ride-details/user-favourite-ride-details';
import { UserPasswordChangeComponent } from './pages/user/user-password-change/user-password-change';

import { GuestComponent } from './pages/guest/guest';

import { RIDE_TRACKING_DS } from './api/user/ride-tracking.datasource';
import { RideTrackingHttpDataSource } from './api/user/ride-tracking.http.datasource';

import { RIDE_RATING_DS } from './api/user/ride-rating.datasource';
import { RideRatingHttpDataSource } from './api/user/ride-rating.http.datasource';

import { RIDE_LIFECYCLE_DS } from './api/driver/ride-lifecycle.datasource';
import { RideLifecycleHttpDataSource } from './api/driver/ride-lifecycle.http.datasource';

// guards
import { authGuard } from './api/auth/auth.guard';
import { roleGuard } from './api/auth/role.guard';

import { ChatHttpDataSource } from './api/chat/chat.http.datasource';
import { CHAT_DS } from './api/chat/chat.datasource';

export const routes: Routes = [
  { path: '', component: LandingComponent },

  { path: 'login', component: LoginComponent },

  /* ===== GUEST (NEREGISTROVANI) ===== */
  { path: 'guest', component: GuestComponent },

  {
    path: 'driver/activate',
    loadComponent: () =>
      import('./pages/driver/driver-activate/driver-activate').then((m) => m.DriverActivate),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./pages/user/reset-password/reset-password').then((m) => m.ResetPassword),
  },
  {
    path: 'new-password',
    loadComponent: () =>
      import('./pages/user/new-password/new-password').then((m) => m.NewPassword),
  },
  {
    path: 'success',
    loadComponent: () =>
      import('./pages/user/success-password/success-password').then((m) => m.SuccessPassword),
  },
  {
    path: 'sign-up',
    loadComponent: () => import('./pages/user/sign-up/sign-up').then((m) => m.SignUp),
  },
  {
    path: 'sign-up-confirmed',
    loadComponent: () =>
      import('./pages/user/sign-up-confirmed/sign-up-confirmed').then((m) => m.SignUpConfirmed),
  },
  {
    path: 'registration-confirm',
    loadComponent: () =>
      import('./pages/user/registration-confirm/registration-confirm').then(
        (m) => m.RegistrationConfirm,
      ),
  },

  // USER
  {
    path: 'user',
    component: UserLayout,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['PASSENGER'] },
    children: [
      {
        path: 'support',
        loadComponent: () => import('./pages/user/user-chat/user-chat').then((m) => m.UserChat),
        providers: [{ provide: CHAT_DS, useClass: ChatHttpDataSource }],
      },
      {
        path: 'home',
        loadComponent: () => import('./pages/user/user-home/user-home').then((m) => m.UserHome),
      },
      {
        path: 'ride-history',
        loadComponent: () =>
          import('./pages/user/user-ride-history/user-ride-history').then((m) => m.UserRideHistory),
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./pages/user/user-reports/user-reports').then((m) => m.UserReports),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./pages/user/user-profile/user-profile').then((m) => m.UserProfile),
      },
      {
        path: 'ride-tracking',
        component: RideTrackingComponent,
        providers: [{ provide: RIDE_TRACKING_DS, useClass: RideTrackingHttpDataSource }],
      },
      {
        path: 'rides/:rideId/rate',
        component: RideRateComponent,
        providers: [{ provide: RIDE_RATING_DS, useClass: RideRatingHttpDataSource }],
      },
      { path: 'order-ride', component: UserOrderRide },
      { path: 'favourite-rides', component: UserFavouriteRides },
      { path: 'favourite-rides/:id', component: UserFavouriteRideDetails },
      { path: 'password-change', component: UserPasswordChangeComponent },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },

  // DRIVER
  {
    path: 'driver',
    component: DriverLayoutComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['DRIVER'] },
    children: [
      {
        path: 'support',
        loadComponent: () => import('./pages/user/user-chat/user-chat').then(m => m.UserChat),
        providers: [{ provide: CHAT_DS, useClass: ChatHttpDataSource }],
      },
      { path: 'home', component: DriverHomeComponent },
      {
        path: 'future-rides',
        loadComponent: () =>
          import('./pages/driver/driver-future-rides/driver-future-rides').then(m => m.DriverFutureRidesComponent),
      },
      {
        path: 'active-ride',
        component: DriverActiveRideComponent,
        providers: [{ provide: RIDE_LIFECYCLE_DS, useClass: RideLifecycleHttpDataSource }],
      },
      { path: 'ride-history', component: DriverRideHistoryComponent },
      { path: 'ride-details/:rideId', component: DriverRideDetailsComponent },
      { path: 'password-change', component: DriverPasswordChangeComponent },
      { path: 'profile', component: DriverProfile },
      {
        path: 'reports',
        loadComponent: () =>
          import('./pages/driver/driver-reports/driver-reports').then((m) => m.DriverReports),
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
    ],
  },

  // ADMIN
  {
    path: 'admin',
    component: AdminLayout,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    children: [
      {
        path: 'ride-status',
        loadComponent: () =>
          import('./pages/admin/admin-ride-status/admin-ride-status').then(
            (m) => m.AdminRideStatus,
          ),
      },
      {
        path: 'pricing',
        loadComponent: () =>
          import('./pages/admin/admin-pricing/admin-pricing').then((m) => m.AdminPricing),
      },
      {
        path: 'reports',
        loadComponent: () =>
          import('./pages/admin/admin-reports/admin-reports').then((m) => m.AdminReports),
      },
      {
        path: 'chats',
        loadComponent: () =>
          import('./pages/admin/admin-chats/admin-chats').then((m) => m.AdminChats),
        providers: [{ provide: CHAT_DS, useClass: ChatHttpDataSource }],
      },
      {
        path: 'chats/:threadId',
        loadComponent: () =>
          import('./pages/admin/admin-chat-details/admin-chat-details').then(
            (m) => m.AdminChatDetails,
          ),
        providers: [{ provide: CHAT_DS, useClass: ChatHttpDataSource }],
      },
      { path: 'home', component: AdminHome },
      { path: 'update-requests', component: AdminUpdateRequests },
      { path: 'create-driver', component: AdminCreateDriver },
      {
        path: 'profile',
        loadComponent: () =>
          import('./pages/admin/admin-profile/admin-profile').then((m) => m.AdminProfile),
      },
      {
        path: 'password-change',
        loadComponent: () =>
          import('./pages/admin/admin-password-change/admin-password-change').then(
            (m) => m.AdminPasswordChange,
          ),
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      {
        path: 'update-requests/:requestId',
        loadComponent: () =>
          import('./pages/admin/admin-update-request-details/admin-update-request-details').then(
            (m) => m.AdminUpdateRequestDetails,
          ),
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
