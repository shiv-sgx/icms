import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { SurveyorApi } from './surveyor.api';
import { SurveyorDashboard } from '../../shared/models';
import { StatusPill } from '../../shared/components/status-pill';
import { Paginator } from '../../shared/components/paginator';

/** Surveyor dashboard — ports surveyor/dashboard.jsp. */
@Component({
  selector: 'app-surveyor-dashboard',
  imports: [RouterLink, StatusPill, Paginator],
  template: `
    <div class="page-head">
      <h1 class="page-title">Surveyor Dashboard</h1>
      <p class="page-sub">{{ user()?.fullName }}</p>
    </div>

    @if (data(); as d) {
      <div class="card-grid">
        <div class="stat-card"><div class="stat-label">Assigned Claims</div><div class="stat-value">{{ d.totalAssigned }}</div></div>
        <div class="stat-card"><div class="stat-label">Pending Survey</div><div class="stat-value">{{ d.pendingSurvey }}</div></div>
        <div class="stat-card"><div class="stat-label">Assessed</div><div class="stat-value">{{ d.assessed }}</div></div>
      </div>

      <div class="panel">
        <div class="panel-head">Assigned Claims</div>
        <div class="panel-body no-pad">
          <table class="table">
            <thead><tr><th>Claim No.</th><th>Type</th><th>Location</th><th>Estimated</th><th>Status</th><th></th></tr></thead>
            <tbody>
              @for (c of d.claims.items; track c.id) {
                <tr>
                  <td>{{ c.claimNo }}</td>
                  <td>{{ c.claimType }}@if (c.claimSubtype) { · {{ c.claimSubtype }} }</td>
                  <td>{{ c.city }} {{ c.state }}</td>
                  <td>&#8377; {{ c.estimatedLoss }}</td>
                  <td><app-status-pill [label]="c.statusLabel" [pillClass]="c.statusPill" /></td>
                  <td><a class="btn btn-primary btn-sm" [routerLink]="['/surveyor/claims', c.id, 'assess']">Assess</a></td>
                </tr>
              } @empty {
                <tr><td colspan="6" class="empty">No claims assigned to you.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      <app-paginator [page]="d.claims.page" [size]="d.claims.size" [total]="d.claims.total" (pageChange)="load($event)" />
    }
  `,
})
export class SurveyorDashboardPage {
  private api = inject(SurveyorApi);
  private auth = inject(AuthService);
  user = this.auth.user;
  data = signal<SurveyorDashboard | null>(null);

  constructor() {
    this.load(1);
  }
  load(p: number): void {
    this.api.dashboard(p, 15).subscribe((d) => this.data.set(d));
  }
}
