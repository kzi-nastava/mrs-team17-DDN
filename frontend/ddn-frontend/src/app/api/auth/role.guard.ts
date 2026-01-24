import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthStore } from './auth.store';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const store = inject(AuthStore);
  const router = inject(Router);

  const token = store.getToken();
  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const role = store.getRoleFromToken(token);
  const allowedRoles = route.data['roles'] as string[];

  if (!allowedRoles || allowedRoles.length === 0) {
    return true;
  }

  if (!role || !allowedRoles.includes(role)) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
