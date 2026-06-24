import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { FlashService } from '../services/flash.service';

/**
 * Central HTTP error handling:
 *  - 401 → clear local session and bounce to /login (token expired/invalid).
 *  - 403 → flash "access denied".
 *  - other 4xx/5xx → flash the server message (validation messages surfaced by forms
 *    can opt out by handling the error locally before this runs is not possible, so
 *    forms read err.error.error.fields directly; this banner is the fallback).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const flash = inject(FlashService);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const serverMsg = err?.error?.error?.message;
      if (err.status === 401) {
        auth.clearLocal();
        // Don't redirect-loop on the login request itself.
        if (!req.url.endsWith('/auth/login')) {
          router.navigate(['/login']);
        }
      } else if (err.status === 403) {
        flash.error('You do not have access to that resource.');
      } else if (err.status === 0) {
        flash.error('Cannot reach the server. Please try again.');
      } else if (err.status >= 500) {
        flash.error(serverMsg || 'Something went wrong. Please try again.');
      }
      return throwError(() => err);
    })
  );
};
