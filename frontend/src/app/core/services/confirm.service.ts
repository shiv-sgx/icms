import { Injectable } from '@angular/core';

/**
 * Confirmation prompt — the SPA replacement for the legacy data-confirm handler
 * in icms.js. Promise-based so callers can `await confirm.ask(...)` before a
 * destructive action (withdraw, delete, reset password). Uses the native dialog
 * for now; can be swapped for a styled modal without changing call sites.
 */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  ask(message: string): Promise<boolean> {
    return Promise.resolve(window.confirm(message));
  }
}
