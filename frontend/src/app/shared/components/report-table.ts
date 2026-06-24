import { Component, input } from '@angular/core';
import { ReportTable } from '../models';

/** Renders a ReportTable (headers + string rows) — used by reports + dashboards. */
@Component({
  selector: 'app-report-table',
  imports: [],
  template: `
    <table class="table">
      <thead><tr>@for (h of report().headers; track $index) { <th>{{ h }}</th> }</tr></thead>
      <tbody>
        @for (row of report().rows; track $index) {
          <tr>@for (cell of row; track $index) { <td>{{ cell }}</td> }</tr>
        } @empty {
          <tr><td [attr.colspan]="report().headers.length" class="empty">No data.</td></tr>
        }
      </tbody>
    </table>
  `,
})
export class ReportTableView {
  report = input.required<ReportTable>();
}
