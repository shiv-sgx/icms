import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApi } from './admin.api';
import { AuditLog, Paged } from '../../shared/models';
import { Paginator } from '../../shared/components/paginator';
import { downloadBlob } from '../../shared/download';

/** Audit logs — ports admin/audit.jsp (CSV export wired in Phase 5). */
@Component({
  selector: 'app-admin-audit',
  imports: [FormsModule, Paginator],
  template: `
    <div class="page-head with-action">
      <div><h1 class="page-title">Audit Logs</h1><p class="page-sub">System activity trail</p></div>
      <button class="btn btn-primary" (click)="exportCsv()">Export CSV</button>
    </div>

    <div class="filter-bar">
      <input type="text" class="input" placeholder="Filter by action (e.g. LOGIN)" [(ngModel)]="action" />
      <select class="input" [(ngModel)]="result">
        <option value="">All results</option>
        <option value="SUCCESS">SUCCESS</option>
        <option value="FAILED">FAILED</option>
      </select>
      <button class="btn btn-primary" (click)="load(1)">Filter</button>
      <button class="btn btn-light" (click)="reset()">Reset</button>
    </div>

    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Timestamp</th><th>User</th><th>Role</th><th>Action</th><th>Entity</th><th>IP</th><th>Result</th></tr></thead>
          <tbody>
            @for (l of page()?.items ?? []; track l.id) {
              <tr>
                <td class="small">{{ l.ts }}</td>
                <td>{{ l.username }}</td>
                <td>{{ l.role }}</td>
                <td>{{ l.action }}</td>
                <td class="small">{{ l.entity }}</td>
                <td class="muted small">{{ l.ipAddress }}</td>
                <td><span class="pill" [class.pill-ok]="l.result === 'SUCCESS'" [class.pill-danger]="l.result !== 'SUCCESS'">{{ l.result }}</span></td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="empty">No audit entries.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>

    @if (page(); as p) {
      <app-paginator [page]="p.page" [size]="p.size" [total]="p.total" (pageChange)="load($event)" />
    }
  `,
})
export class AdminAuditPage {
  private api = inject(AdminApi);
  action = '';
  result = '';
  page = signal<Paged<AuditLog> | null>(null);
  constructor() {
    this.load(1);
  }
  load(p: number): void {
    this.api.audit(this.action, this.result, p, 15).subscribe((res) => this.page.set(res));
  }
  reset(): void {
    this.action = '';
    this.result = '';
    this.load(1);
  }
  exportCsv(): void {
    this.api.exportAudit(this.action, this.result).subscribe((blob) => downloadBlob('audit-logs.csv', blob));
  }
}
