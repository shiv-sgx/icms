import { Injectable, signal } from '@angular/core';

export type FlashType = 'success' | 'error';
export interface Flash {
  type: FlashType;
  message: string;
}

/**
 * App-wide flash banner — the SPA counterpart of the Struts session flash
 * (POST-redirect-GET). Rendered by the app-shell using .alert / .alert-success /
 * .alert-error classes from icms.css.
 */
@Injectable({ providedIn: 'root' })
export class FlashService {
  readonly current = signal<Flash | null>(null);
  private timer: ReturnType<typeof setTimeout> | null = null;

  show(type: FlashType, message: string, ttlMs = 5000): void {
    this.current.set({ type, message });
    if (this.timer) clearTimeout(this.timer);
    this.timer = setTimeout(() => this.current.set(null), ttlMs);
  }

  success(message: string): void {
    this.show('success', message);
  }
  error(message: string): void {
    this.show('error', message);
  }
  clear(): void {
    this.current.set(null);
    if (this.timer) clearTimeout(this.timer);
  }
}
