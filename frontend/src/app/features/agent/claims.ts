import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AgentApi } from './agent.api';
import { Claim, Paged } from '../../shared/models';
import { StatusPill } from '../../shared/components/status-pill';
import { Paginator } from '../../shared/components/paginator';

const STATUSES = [
  'SUBMITTED', 'UNDER_REVIEW', 'SURVEY_SCHEDULED', 'UNDER_ASSESSMENT', 'PENDING_APPROVAL',
  'APPROVED', 'SETTLEMENT_PROCESSING', 'SETTLED', 'CLOSED', 'REJECTED', 'WITHDRAWN', 'ON_HOLD',
];
const TYPES = ['MOTOR', 'HEALTH', 'PROPERTY', 'LIFE', 'TRAVEL', 'LIABILITY'];

/** Agent claims management — ports agent/claims.jsp (filter bar + table). */
@Component({
  selector: 'app-agent-claims',
  imports: [RouterLink, FormsModule, StatusPill, Paginator],
  template: `
    <div class="page-head">
      <h1 class="page-title">Claims Management</h1>
      <p class="page-sub">Search, filter, and action claims</p>
    </div>

    <div class="filter-bar">
      <input type="text" class="input" placeholder="Search claim no. or claimant..." [(ngModel)]="q" />
      <select class="input" [(ngModel)]="status">
        <option value="">All statuses</option>
        @for (s of statuses; track s) { <option [value]="s">{{ s }}</option> }
      </select>
      <select class="input" [(ngModel)]="type">
        <option value="">All types</option>
        @for (t of types; track t) { <option [value]="t">{{ t }}</option> }
      </select>
      <button class="btn btn-primary" (click)="load(1)">Filter</button>
      <button class="btn btn-light" (click)="reset()">Reset</button>
    </div>

    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Claim No.</th><th>Claimant</th><th>Type</th><th>Estimated</th><th>Surveyor</th><th>Risk</th><th>Status</th></tr></thead>
          <tbody>
            @for (c of page()?.items ?? []; track c.id) {
              <tr>
                <td><a [routerLink]="['/agent/claims', c.id]">{{ c.claimNo }}</a></td>
                <td>{{ c.claimantName }}</td>
                <td>{{ c.claimType }}</td>
                <td>&#8377; {{ c.estimatedLoss }}</td>
                <td>{{ c.surveyorName || '—' }}</td>
                <td><span class="pill risk-{{ c.riskLevel }}">{{ c.riskLevel }}</span></td>
                <td><app-status-pill [label]="c.statusLabel" [pillClass]="c.statusPill" /></td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="empty">No claims match your filters.</td></tr>
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
export class AgentClaimsPage {
  private api = inject(AgentApi);
  statuses = STATUSES;
  types = TYPES;
  q = '';
  status = '';
  type = '';
  page = signal<Paged<Claim> | null>(null);

  constructor() {
    this.load(1);
  }

  load(p: number): void {
    this.api.claims({ status: this.status, type: this.type, q: this.q, page: p, size: 15 }).subscribe((res) =>
      this.page.set(res)
    );
  }

  reset(): void {
    this.q = '';
    this.status = '';
    this.type = '';
    this.load(1);
  }
}
