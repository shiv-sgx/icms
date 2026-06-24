import { Component, input } from '@angular/core';

/** Status/risk pill — renders the same .pill markup the JSPs used. */
@Component({
  selector: 'app-status-pill',
  imports: [],
  template: `<span class="pill {{ pillClass() }}">{{ label() }}</span>`,
})
export class StatusPill {
  label = input.required<string>();
  pillClass = input<string>('pill-muted');
}
