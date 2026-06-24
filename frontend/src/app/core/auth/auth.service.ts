import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser, ApiEnvelope, LoginResponse, Role } from '../../shared/models';
import { TokenStorage } from './token-storage';

/** Role → landing route (mirrors Roles.dashboardFor in the Struts app). */
const DASHBOARDS: Record<Role, string> = {
  CUSTOMER: '/customer/dashboard',
  AGENT: '/agent/dashboard',
  SURVEYOR: '/surveyor/dashboard',
  MANAGER: '/manager/dashboard',
  ADMIN: '/admin/dashboard',
};

interface JwtClaims {
  sub: string;
  username: string;
  role: Role;
  fullName: string;
  exp: number;
}

function decodeJwt(token: string): JwtClaims | null {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private storage = inject(TokenStorage);

  private readonly _user = signal<AuthUser | null>(this.userFromToken());
  readonly user = this._user.asReadonly();
  readonly isLoggedIn = computed(() => this._user() !== null);
  readonly role = computed<Role | null>(() => this._user()?.role ?? null);

  private userFromToken(): AuthUser | null {
    const token = this.storage.get();
    if (!token) return null;
    const claims = decodeJwt(token);
    if (!claims || claims.exp * 1000 <= Date.now()) {
      this.storage.clear();
      return null;
    }
    return {
      id: Number(claims.sub),
      username: claims.username,
      role: claims.role,
      fullName: claims.fullName,
    };
  }

  login(username: string, password: string): Observable<ApiEnvelope<LoginResponse>> {
    return this.http
      .post<ApiEnvelope<LoginResponse>>(`${environment.apiBaseUrl}/auth/login`, { username, password })
      .pipe(
        tap((res) => {
          this.storage.set(res.data.token);
          this._user.set(res.data.user);
        })
      );
  }

  /** Best-effort server audit, then clear local state regardless. */
  logout(): void {
    this.http.post(`${environment.apiBaseUrl}/auth/logout`, {}).subscribe({
      next: () => {},
      error: () => {},
    });
    this.clearLocal();
  }

  /** Clears local session without calling the server (used by the 401 interceptor). */
  clearLocal(): void {
    this.storage.clear();
    this._user.set(null);
  }

  token(): string | null {
    return this.storage.get();
  }

  dashboardFor(role: Role): string {
    return DASHBOARDS[role] ?? '/login';
  }
}
