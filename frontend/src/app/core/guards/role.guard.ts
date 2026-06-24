import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { Role } from '../../shared/models';

/**
 * Role gate (UX only — the server roleGuard is the real enforcement). Allows the
 * route when the user's role matches route.data.role, OR the user is ADMIN
 * (ADMIN can access any namespace) — same rule as the Struts RoleInterceptor.
 */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const required = route.data?.['role'] as Role | undefined;
  const role = auth.role();

  if (!role) return router.createUrlTree(['/login']);
  if (!required || role === required || role === 'ADMIN') return true;
  return router.createUrlTree(['/denied']);
};
