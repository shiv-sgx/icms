import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

/**
 * Phase 0 placeholder landing — verifies the reused icms.css design system and
 * the dev API proxy (/api -> Node) render correctly. Replaced by the login page
 * and role shells in Phase 1.
 */
@Component({
  selector: 'app-landing',
  imports: [],
  template: `
    <div class="public-body">
      <div class="public-wrap">
        <div class="brand-mark">
          <span class="brand-logo">ICMS</span>
          <span class="brand-sub">Insurance Claims Management System</span>
        </div>
        <div class="login-card">
          <h1 class="login-title">MEAN migration — Phase 0</h1>
          <p class="login-hint">Angular shell is live and using the original ICMS stylesheet.</p>
          <p class="small muted">
            API health:
            <span class="pill" [class.pill-ok]="apiUp() === true" [class.pill-danger]="apiUp() === false"
                  [class.pill-muted]="apiUp() === null">
              {{ apiUp() === null ? 'checking…' : (apiUp() ? 'UP' : 'DOWN') }}
            </span>
          </p>
        </div>
        <p class="login-foot">© ICMS</p>
      </div>
    </div>
  `,
})
export class Landing {
  private http = inject(HttpClient);
  apiUp = signal<boolean | null>(null);

  constructor() {
    this.http.get(`${environment.apiBaseUrl}/health`).subscribe({
      next: () => this.apiUp.set(true),
      error: () => this.apiUp.set(false),
    });
  }
}
