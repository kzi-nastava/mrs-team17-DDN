import { Routes } from '@angular/router';
import { LandingComponent } from './pages/landing/landing';
import { DriverRideHistoryComponent } from './pages/driver/driver-ride-history/driver-ride-history';
import { DriverRideDetailsComponent } from './pages/driver/driver-ride-details/driver-ride-details';
import { DriverHomeComponent } from './pages/driver/driver-home/driver-home';
import { DriverLayoutComponent } from './pages/driver/driver-layout/driver-layout';
import { DriverProfile } from './pages/driver/driver-profile/driver-profile';




export const routes: Routes = [
  { path: '', component: LandingComponent },

  {
    path: 'driver',
    component: DriverLayoutComponent,
    children: [
      { path: 'home', component: DriverHomeComponent },
      { path: 'ride-history', component: DriverRideHistoryComponent },
      { path: 'ride-details/:id', component: DriverRideDetailsComponent },
      { path: 'profile', component: DriverProfile },
    ],
  },
];
