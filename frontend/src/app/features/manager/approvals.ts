import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ManagerApi } from './manager.api';
import { Claim, Paged } from '../../shared/models';
import { Paginator } from '../../shared/components/paginator';

/** Approval queue — ports manager/approvals.jsp. */
@Component({
  selector: 'app-manager-approvals',
  imports: [RouterLink, Paginator],
  template: `
    <div class="page-head">
      <h1 class="page-title">Approval Queue</h1>
      <p class="page-sub">Claims awaiting your decision</p>
    </div>

    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Claim No.</th><th>Claimant</th><th>Type</th><th>Estimated</th><th>Risk</th><th>Fraud</th><th></th></tr></thead>
          <tbody>
            @for (c of page()?.items ?? []; track c.id) {
              <tr>
                <td>{{ c.claimNo }}</td>
                <td>{{ c.claimantName }}</td>
                <td>{{ c.claimType }}</td>
                <td>&#8377; {{ c.estimatedLoss }}</td>
                <td><span class="pill risk-{{ c.riskLevel }}">{{ c.riskLevel }}</span></td>
                <td>{{ c.fraudScore }}</td>
                <td><a class="btn btn-primary btn-sm" [routerLink]="['/manager/claims', c.id]">Review</a></td>
              </tr>
            } @empty {
              <tr><td colspan="7" class="empty">No claims awaiting approval.</td></tr>
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
export class ManagerApprovalsPage {
  private api = inject(ManagerApi);
  page = signal<Paged<Claim> | null>(null);
  constructor() {
    this.load(1);
  }
  load(p: number): void {
    this.api.approvals(p, 15).subscribe((res) => this.page.set(res));
  }
}
