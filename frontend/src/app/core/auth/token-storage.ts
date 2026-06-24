import { Injectable } from '@angular/core';

/**
 * Persists the JWT. sessionStorage by default (cleared on tab close, lower XSS
 * persistence than localStorage). Centralised so the storage choice lives in one place.
 */
const KEY = 'icms.token';

@Injectable({ providedIn: 'root' })
export class TokenStorage {
  get(): string | null {
    return sessionStorage.getItem(KEY);
  }
  set(token: string): void {
    sessionStorage.setItem(KEY, token);
  }
  clear(): void {
    sessionStorage.removeItem(KEY);
  }
}
