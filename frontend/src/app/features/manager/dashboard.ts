import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { ManagerApi } from './manager.api';
import { ManagerDashboard } from '../../shared/models';
import { ReportTableView } from '../../shared/components/report-table';

/** Manager dashboard — ports manager/dashboard.jsp. */
@Component({
  selector: 'app-manager-dashboard',
  imports: [RouterLink, ReportTableView],
  template: `
    <div class="page-head">
      <h1 class="page-title">Manager Dashboard</h1>
      <p class="page-sub">{{ user()?.fullName }}</p>
    </div>

    @if (data(); as d) {
      <div class="card-grid">
        <div class="stat-card"><div class="stat-label">Pending Approval</div><div class="stat-value">{{ d.pendingApproval }}</div></div>
        <div class="stat-card"><div class="stat-label">High Risk</div><div class="stat-value">{{ d.highRisk }}</div></div>
        <div class="stat-card"><div class="stat-label">SLA Breaches</div><div class="stat-value">{{ d.slaBreaches }}</div></div>
        <div class="stat-card"><div class="stat-label">Settled</div><div class="stat-value">{{ d.settled }}</div></div>
      </div>

      <div class="grid-2">
        <div class="panel">
          <div class="panel-head">Approval Queue <a routerLink="/manager/approvals" class="link-more">View all</a></div>
          <div class="panel-body no-pad">
            <table class="table">
              <thead><tr><th>Claim</th><th>Type</th><th>Estimated</th><th>Risk</th></tr></thead>
              <tbody>
                @for (c of d.queue; track c.id) {
                  <tr>
                    <td><a [routerLink]="['/manager/claims', c.id]">{{ c.claimNo }}</a></td>
                    <td>{{ c.claimType }}</td>
                    <td>&#8377; {{ c.estimatedLoss }}</td>
                    <td><span class="pill risk-{{ c.riskLevel }}">{{ c.riskLevel }}</span></td>
                  </tr>
                } @empty {
                  <tr><td colspan="4" class="empty">No claims awaiting approval.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">Agent Performance <a routerLink="/manager/reports" class="link-more">Reports</a></div>
          <div class="panel-body no-pad">
            <app-report-table [report]="d.agentPerformance" />
          </div>
        </div>
      </div>
    }
  `,
})
export class ManagerDashboardPage {
  private api = inject(ManagerApi);
  private auth = inject(AuthService);
  user = this.auth.user;
  data = signal<ManagerDashboard | null>(null);
  constructor() {
    this.api.dashboard().subscribe((d) => this.data.set(d));
  }
}
