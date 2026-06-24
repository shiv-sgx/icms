import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { CustomerApi } from './customer.api';
import { CustomerDashboard } from '../../shared/models';
import { StatusPill } from '../../shared/components/status-pill';

/** Customer dashboard — ports customer/dashboard.jsp. */
@Component({
  selector: 'app-customer-dashboard',
  imports: [RouterLink, StatusPill],
  template: `
    <div class="page-head">
      <h1 class="page-title">Welcome, {{ user()?.fullName }}</h1>
      <p class="page-sub">Customer Portal</p>
    </div>

    @if (data(); as d) {
      @if (!d.hasProfile) {
        <div class="alert alert-error">
          No policyholder profile is linked to your account. Please contact support.
        </div>
      }

      <div class="card-grid">
        <div class="stat-card"><div class="stat-label">Total Claims</div><div class="stat-value">{{ d.totalClaims }}</div></div>
        <div class="stat-card"><div class="stat-label">Open Claims</div><div class="stat-value">{{ d.openClaims }}</div></div>
        <div class="stat-card"><div class="stat-label">Settled</div><div class="stat-value">{{ d.settledClaims }}</div></div>
        <div class="stat-card action-card">
          <div class="stat-label">Need to file a claim?</div>
          <a routerLink="/customer/new-claim" class="btn btn-primary">+ New Claim</a>
        </div>
      </div>

      <div class="grid-2">
        <div class="panel">
          <div class="panel-head">Recent Claims <a routerLink="/customer/claims" class="link-more">View all</a></div>
          <div class="panel-body no-pad">
            @if (d.recentClaims.length === 0) {
              <p class="empty">No claims yet. <a routerLink="/customer/new-claim">File your first claim</a>.</p>
            } @else {
              <table class="table">
                <thead><tr><th>Claim</th><th>Type</th><th>Filed</th><th>Status</th></tr></thead>
                <tbody>
                  @for (c of d.recentClaims; track c.id) {
                    <tr>
                      <td><a [routerLink]="['/customer/claims', c.id]">{{ c.claimNo }}</a></td>
                      <td>{{ c.claimType }}</td>
                      <td>{{ c.filedAt }}</td>
                      <td><app-status-pill [label]="c.statusLabel" [pillClass]="c.statusPill" /></td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">Notifications</div>
          <div class="panel-body no-pad">
            @if (d.notifications.length === 0) {
              <p class="empty">You're all caught up.</p>
            } @else {
              <ul class="notif-list">
                @for (n of d.notifications; track n.id) {
                  <li class="notif-item notif-{{ n.type }}">
                    <span class="notif-dot"></span>
                    <span class="notif-msg">{{ n.message }}</span>
                  </li>
                }
              </ul>
            }
          </div>
        </div>
      </div>
    }
  `,
})
export class CustomerDashboardPage {
  private api = inject(CustomerApi);
  private auth = inject(AuthService);
  user = this.auth.user;
  data = signal<CustomerDashboard | null>(null);

  constructor() {
    this.api.dashboard().subscribe((d) => this.data.set(d));
  }
}
