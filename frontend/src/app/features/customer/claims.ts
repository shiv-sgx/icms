import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CustomerApi } from './customer.api';
import { Claim, Paged } from '../../shared/models';
import { StatusPill } from '../../shared/components/status-pill';
import { Paginator } from '../../shared/components/paginator';

/** Customer claims list — ports customer/claims.jsp. */
@Component({
  selector: 'app-customer-claims',
  imports: [RouterLink, StatusPill, Paginator],
  template: `
    <div class="page-head with-action">
      <div>
        <h1 class="page-title">My Claims</h1>
        <p class="page-sub">All claims filed under your policies</p>
      </div>
      <a routerLink="/customer/new-claim" class="btn btn-primary">+ New Claim</a>
    </div>

    <div class="panel">
      <div class="panel-body no-pad">
        @if (!page() || page()!.items.length === 0) {
          <p class="empty">No claims found. <a routerLink="/customer/new-claim">File a new claim</a>.</p>
        } @else {
          <table class="table">
            <thead>
              <tr><th>Claim No.</th><th>Type</th><th>Policy</th><th>Incident</th><th>Estimated</th><th>Status</th></tr>
            </thead>
            <tbody>
              @for (c of page()!.items; track c.id) {
                <tr>
                  <td><a [routerLink]="['/customer/claims', c.id]">{{ c.claimNo }}</a></td>
                  <td>{{ c.claimType }}@if (c.claimSubtype) { · {{ c.claimSubtype }} }</td>
                  <td>{{ c.policyNo }}</td>
                  <td>{{ c.incidentDate }}</td>
                  <td>&#8377; {{ c.estimatedLoss }}</td>
                  <td><app-status-pill [label]="c.statusLabel" [pillClass]="c.statusPill" /></td>
                </tr>
              }
            </tbody>
          </table>
        }
      </div>
    </div>

    @if (page(); as p) {
      <app-paginator [page]="p.page" [size]="p.size" [total]="p.total" (pageChange)="load($event)" />
    }
  `,
})
export class CustomerClaimsPage {
  private api = inject(CustomerApi);
  page = signal<Paged<Claim> | null>(null);
  size = 15;

  constructor() {
    this.load(1);
  }

  load(p: number): void {
    this.api.claims(p, this.size).subscribe((res) => this.page.set(res));
  }
}
