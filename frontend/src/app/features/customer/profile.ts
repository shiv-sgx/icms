import { Component, inject, signal } from '@angular/core';
import { CustomerApi, ProfileResponse } from './customer.api';

/** Customer profile — ports customer/profile.jsp (read-only, as in the Struts app). */
@Component({
  selector: 'app-customer-profile',
  imports: [],
  template: `
    <div class="page-head">
      <h1 class="page-title">My Profile</h1>
      <p class="page-sub">Account and policy details</p>
    </div>

    @if (data(); as d) {
      <div class="grid-2">
        <div class="panel">
          <div class="panel-head">Account</div>
          <div class="panel-body">
            <dl class="kv">
              <dt>Name</dt><dd>{{ d.account.fullName }}</dd>
              <dt>Username</dt><dd>{{ d.account.username }}</dd>
              <dt>Email</dt><dd>{{ d.account.email }}</dd>
            </dl>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">Policyholder</div>
          <div class="panel-body">
            @if (d.hasProfile && d.policyholder) {
              <dl class="kv">
                <dt>Name</dt><dd>{{ d.policyholder.fullName }}</dd>
                <dt>Mobile</dt><dd>{{ d.policyholder.mobile }}</dd>
                <dt>Address</dt><dd>{{ d.policyholder.address }}, {{ d.policyholder.city }}, {{ d.policyholder.state }} {{ d.policyholder.pinCode }}</dd>
              </dl>
            } @else {
              <p class="muted">No policyholder profile linked.</p>
            }
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">My Policies</div>
        <div class="panel-body no-pad">
          <table class="table">
            <thead><tr><th>Policy No.</th><th>Type</th><th>Sum Insured</th><th>Expiry</th><th>Status</th></tr></thead>
            <tbody>
              @for (p of d.policies; track p.id) {
                <tr>
                  <td>{{ p.policyNo }}</td>
                  <td>{{ p.type }}</td>
                  <td>&#8377; {{ p.sumInsured }}</td>
                  <td>{{ p.expiryDate }}</td>
                  <td><span class="pill pill-ok">{{ p.status }}</span></td>
                </tr>
              } @empty {
                <tr><td colspan="5" class="empty">No policies on record.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }
  `,
})
export class CustomerProfilePage {
  private api = inject(CustomerApi);
  data = signal<ProfileResponse | null>(null);

  constructor() {
    this.api.profile().subscribe((d) => this.data.set(d));
  }
}
