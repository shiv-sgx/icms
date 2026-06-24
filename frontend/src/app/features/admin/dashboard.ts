import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AdminApi } from './admin.api';
import { AdminStats } from '../../shared/models';

/** Admin console — ports admin/dashboard.jsp (Hikari stats -> Node pool stats). */
@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink],
  template: `
    <div class="page-head">
      <h1 class="page-title">Admin Console</h1>
      <p class="page-sub">System administration &amp; monitoring</p>
    </div>

    @if (stats(); as s) {
      <div class="card-grid">
        <div class="stat-card"><div class="stat-label">Users</div><div class="stat-value">{{ s.users }}</div></div>
        <div class="stat-card"><div class="stat-label">Claims</div><div class="stat-value">{{ s.claims }}</div></div>
        <div class="stat-card"><div class="stat-label">Roles</div><div class="stat-value">{{ s.roles }}</div></div>
        <div class="stat-card"><div class="stat-label">Audit Events</div><div class="stat-value">{{ s.auditEvents }}</div></div>
      </div>

      <div class="panel">
        <div class="panel-head">Database Connection Pool</div>
        <div class="panel-body">
          <div class="card-grid">
            <div class="stat-card"><div class="stat-label">Active</div><div class="stat-value">{{ s.poolActive }}</div></div>
            <div class="stat-card"><div class="stat-label">Idle</div><div class="stat-value">{{ s.poolIdle }}</div></div>
            <div class="stat-card"><div class="stat-label">Total</div><div class="stat-value">{{ s.poolTotal }}</div></div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">Administration</div>
        <div class="panel-body">
          <div class="quick-links">
            <a routerLink="/admin/users" class="btn btn-light">User Management</a>
            <a routerLink="/admin/roles" class="btn btn-light">Role Management</a>
            <a routerLink="/admin/documents" class="btn btn-light">Claim Config</a>
            <a routerLink="/admin/sla" class="btn btn-light">SLA Config</a>
            <a routerLink="/admin/thresholds" class="btn btn-light">Approval Thresholds</a>
            <a routerLink="/admin/templates" class="btn btn-light">Notification Templates</a>
            <a routerLink="/admin/audit" class="btn btn-light">Audit Logs</a>
          </div>
        </div>
      </div>
    }
  `,
})
export class AdminDashboardPage {
  private api = inject(AdminApi);
  stats = signal<AdminStats | null>(null);
  constructor() {
    this.api.dashboard().subscribe((s) => this.stats.set(s));
  }
}
