import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

/**
 * Temporary page for routes whose real components arrive in a later phase. Reads
 * its title/subtitle from route data so the sidebar nav resolves and the shell
 * renders correctly during Phase 1. Replaced phase-by-phase.
 */
@Component({
  selector: 'app-page-placeholder',
  imports: [],
  template: `
    <div class="page-head">
      <h1 class="page-title">{{ title }}</h1>
      <p class="page-sub">{{ subtitle }}</p>
    </div>
    <div class="panel">
      <div class="panel-body">
        <p class="empty">This screen is being migrated and will be available in an upcoming phase.</p>
      </div>
    </div>
  `,
})
export class PagePlaceholder {
  private route = inject(ActivatedRoute);
  title = this.route.snapshot.data['title'] ?? 'Coming soon';
  subtitle = this.route.snapshot.data['subtitle'] ?? '';
}
