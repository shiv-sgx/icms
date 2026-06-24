import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { AgentApi } from './agent.api';
import { AgentDashboard } from '../../shared/models';
import { StatusPill } from '../../shared/components/status-pill';

/** Agent dashboard — ports agent/dashboard.jsp. */
@Component({
  selector: 'app-agent-dashboard',
  imports: [RouterLink, StatusPill],
  template: `
    <div class="page-head">
      <h1 class="page-title">Agent Dashboard</h1>
      <p class="page-sub">{{ user()?.fullName }}</p>
    </div>

    @if (data(); as d) {
      <div class="card-grid">
        <div class="stat-card"><div class="stat-label">Open Claims</div><div class="stat-value">{{ d.openClaims }}</div></div>
        <div class="stat-card"><div class="stat-label">Awaiting Survey</div><div class="stat-value">{{ d.awaitingSurvey }}</div></div>
        <div class="stat-card"><div class="stat-label">Pending Approval</div><div class="stat-value">{{ d.pendingApproval }}</div></div>
        <div class="stat-card"><div class="stat-label">Settled</div><div class="stat-value">{{ d.settled }}</div></div>
      </div>

      <div class="panel">
        <div class="panel-head">Worklist <a routerLink="/agent/claims" class="link-more">All claims</a></div>
        <div class="panel-body no-pad">
          <table class="table">
            <thead><tr><th>Claim No.</th><th>Claimant</th><th>Type</th><th>Filed</th><th>Risk</th><th>Status</th></tr></thead>
            <tbody>
              @for (c of d.worklist; track c.id) {
                <tr>
                  <td><a [routerLink]="['/agent/claims', c.id]">{{ c.claimNo }}</a></td>
                  <td>{{ c.claimantName }}</td>
                  <td>{{ c.claimType }}</td>
                  <td>{{ c.filedAt }}</td>
                  <td><span class="pill risk-{{ c.riskLevel }}">{{ c.riskLevel }}</span></td>
                  <td><app-status-pill [label]="c.statusLabel" [pillClass]="c.statusPill" /></td>
                </tr>
              } @empty {
                <tr><td colspan="6" class="empty">No claims need attention right now.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }
  `,
})
export class AgentDashboardPage {
  private api = inject(AgentApi);
  private auth = inject(AuthService);
  user = this.auth.user;
  data = signal<AgentDashboard | null>(null);
  constructor() {
    this.api.dashboard().subscribe((d) => this.data.set(d));
  }
}
