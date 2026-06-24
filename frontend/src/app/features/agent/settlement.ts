import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AgentApi } from './agent.api';
import { SettlementScreen, TimelineStage } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';
import { Timeline } from '../../shared/components/timeline';

/** Agent settlement screen — ports agent/settlement.jsp. */
@Component({
  selector: 'app-agent-settlement',
  imports: [RouterLink, FormsModule, Timeline],
  template: `
    @if (screen(); as s) {
      <div class="page-head with-action">
        <div>
          <h1 class="page-title">Settlement — {{ s.claim.claimNo }}</h1>
          <p class="page-sub">{{ s.claim.claimantName }} &middot; {{ s.claim.claimType }} &middot; Policy {{ s.claim.policyNo }}</p>
        </div>
        <a class="btn btn-light" [routerLink]="['/agent/claims', s.claim.id]">&laquo; Back to claim</a>
      </div>

      @if (s.settlement; as st) {
        <div class="panel">
          <div class="panel-head">Payment Status Tracker</div>
          <div class="panel-body"><app-timeline [stages]="trackerStages()" /></div>
        </div>

        <div class="grid-2">
          <div class="panel">
            <div class="panel-head">Settlement Summary</div>
            <div class="panel-body">
              <dl class="kv">
                <dt>Final Amount</dt><dd class="big">&#8377; {{ st.finalAmount }}</dd>
                <dt>Status</dt><dd><span class="pill pill-info">{{ st.status }}</span></dd>
                <dt>Method</dt><dd>{{ st.paymentMethod }}</dd>
                <dt>Justification</dt><dd>{{ st.justification }}</dd>
              </dl>
            </div>
          </div>
          <div class="panel">
            <div class="panel-head">Payment Details</div>
            <div class="panel-body">
              <dl class="kv">
                <dt>Account Holder</dt><dd>{{ st.accountHolder }}</dd>
                <dt>Bank</dt><dd>{{ st.bankName }}</dd>
                <dt>Account No.</dt><dd>{{ st.accountNumber }}</dd>
                <dt>IFSC</dt><dd>{{ st.ifscCode }}</dd>
              </dl>
              @if (st.status !== 'CLOSED') {
                <button class="btn btn-primary" (click)="advance(s.claim.id)">Advance Payment Status &raquo;</button>
              }
            </div>
          </div>
        </div>
      } @else if (s.claim.status === 'APPROVED') {
        <div class="panel">
          <div class="panel-head">Authorise Settlement</div>
          <div class="panel-body">
            <form class="form-card" (ngSubmit)="authorize(s.claim.id)">
              <div class="form-row">
                <div class="field"><label>Settlement Amount (&#8377;)</label><input type="number" step="0.01" min="0" class="input" name="amount" [(ngModel)]="form.amount" required /></div>
                <div class="field">
                  <label>Payment Method</label>
                  <select class="input" name="paymentMethod" [(ngModel)]="form.paymentMethod">
                    @for (m of s.paymentMethods; track m) { <option [value]="m">{{ m }}</option> }
                  </select>
                </div>
              </div>
              <div class="form-row">
                <div class="field"><label>Account Holder</label><input type="text" class="input" name="accountHolder" [(ngModel)]="form.accountHolder" /></div>
                <div class="field"><label>Bank Name</label><input type="text" class="input" name="bankName" [(ngModel)]="form.bankName" /></div>
              </div>
              <div class="form-row">
                <div class="field"><label>Account Number</label><input type="text" class="input" name="accountNumber" [(ngModel)]="form.accountNumber" /></div>
                <div class="field"><label>IFSC Code</label><input type="text" class="input" name="ifscCode" [(ngModel)]="form.ifscCode" /></div>
              </div>
              <div class="field"><label>Justification</label><input type="text" class="input" name="justification" [(ngModel)]="form.justification" placeholder="Basis for the settlement amount" /></div>
              <div class="form-actions"><button type="submit" class="btn btn-primary">Authorise Settlement</button></div>
            </form>
          </div>
        </div>
      } @else {
        <div class="alert alert-error">This claim is not yet approved for settlement (current status: {{ s.claim.statusLabel }}).</div>
      }
    }
  `,
})
export class AgentSettlementPage {
  private api = inject(AgentApi);
  private flash = inject(FlashService);

  id = input.required<string>();
  screen = signal<SettlementScreen | null>(null);
  form = {
    amount: '',
    paymentMethod: 'NEFT',
    accountHolder: '',
    bankName: '',
    accountNumber: '',
    ifscCode: '',
    justification: '',
  };

  trackerStages = computed<TimelineStage[]>(() => {
    const s = this.screen();
    if (!s || !s.settlement) return [];
    const cur = s.tracker.indexOf(s.settlement.status);
    return s.tracker.map((label, i) => ({
      label: label.replace(/_/g, ' '),
      state: i < cur ? 'done' : i === cur ? 'current' : 'pending',
    }));
  });

  constructor() {
    queueMicrotask(() => this.reload());
  }

  private reload(): void {
    this.api.settlement(Number(this.id())).subscribe((s) => {
      this.screen.set(s);
      if (!s.settlement) {
        this.form.amount = s.suggestedAmount;
        this.form.accountHolder = s.claim.claimantName;
      }
    });
  }

  authorize(id: number): void {
    this.api.authorizeSettlement(id, { ...this.form }).subscribe(() => {
      this.flash.success('Settlement authorised. Payment processing started.');
      this.reload();
    });
  }

  advance(id: number): void {
    this.api.advanceSettlement(id).subscribe(() => {
      this.flash.success('Payment status advanced.');
      this.reload();
    });
  }
}
