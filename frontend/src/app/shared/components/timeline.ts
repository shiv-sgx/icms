import { Component, input } from '@angular/core';
import { TimelineStage } from '../models';

/** Claim status timeline — ports the <ol class="timeline"> block from claim-detail.jsp. */
@Component({
  selector: 'app-timeline',
  imports: [],
  template: `
    <ol class="timeline">
      @for (s of stages(); track $index) {
        <li class="tl-node tl-{{ s.state }}">
          <span class="tl-dot"></span>
          <span class="tl-label">{{ s.label }}</span>
        </li>
      }
    </ol>
  `,
})
export class Timeline {
  stages = input.required<TimelineStage[]>();
}
