import { Component, inject, signal } from '@angular/core';
import { ManagerApi } from './manager.api';
import { ReportTable } from '../../shared/models';
import { ReportTableView } from '../../shared/components/report-table';

/** Reports & Analytics — ports manager/reports.jsp (CSV export wired in Phase 5). */
@Component({
  selector: 'app-manager-reports',
  imports: [ReportTableView],
  template: `
    <div class="page-head">
      <h1 class="page-title">Reports &amp; Analytics</h1>
      <p class="page-sub">Operational insights</p>
    </div>

    @for (r of reports(); track r.key) {
      <div class="panel">
        <div class="panel-head">{{ r.title }}</div>
        <div class="panel-body no-pad">
          <app-report-table [report]="r" />
        </div>
      </div>
    }
  `,
})
export class ManagerReportsPage {
  private api = inject(ManagerApi);
  reports = signal<ReportTable[]>([]);
  constructor() {
    this.api.reports().subscribe((r) => this.reports.set(r));
  }
}
