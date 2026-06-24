import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorage } from '../auth/token-storage';
import { environment } from '../../../environments/environment';

/** Attaches the Bearer token to API requests (skips the login call itself). */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(TokenStorage);
  const token = storage.get();
  const isApi = req.url.startsWith(environment.apiBaseUrl);
  const isLogin = req.url.endsWith('/auth/login');

  if (token && isApi && !isLogin) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
