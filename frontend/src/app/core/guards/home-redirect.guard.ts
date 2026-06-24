import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * Root ('/') redirect: send a logged-in user to their role dashboard, otherwise
 * to /login. Mirrors the role-based landing the Struts LoginAction performed.
 */
export const homeRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const role = auth.role();
  return router.createUrlTree([role ? auth.dashboardFor(role) : '/login']);
};
