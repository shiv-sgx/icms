import { Component, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ManagerApi } from './manager.api';
import { AgentBundle } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { StatusPill } from '../../shared/components/status-pill';
import { Timeline } from '../../shared/components/timeline';

/** Manager claim detail — ports manager/claim-detail.jsp (decision + override). */
@Component({
  selector: 'app-manager-claim-detail',
  imports: [RouterLink, FormsModule, StatusPill, Timeline],
  template: `
    @if (bundle(); as b) {
      <div class="page-head with-action">
        <div>
          <h1 class="page-title">
            {{ b.claim.claimNo }}
            <app-status-pill [label]="b.claim.statusLabel" [pillClass]="b.claim.statusPill" />
            <span class="pill risk-{{ b.claim.riskLevel }}">{{ b.claim.riskLevel }} RISK</span>
          </h1>
          <p class="page-sub">{{ b.claim.claimType }} &middot; {{ b.claim.claimantName }} &middot; Fraud score {{ b.claim.fraudScore }}/100</p>
        </div>
        <a routerLink="/manager/approvals" class="btn btn-light">&laquo; Queue</a>
      </div>

      @if (b.claim.status === 'PENDING_APPROVAL') {
        <div class="panel decision-panel">
          <div class="panel-head">Approval Decision</div>
          <div class="panel-body">
            <form class="decision-form" (ngSubmit)="decide(b.claim.id)">
              <div class="form-row">
                <div class="field">
                  <label>Decision</label>
                  <select class="input" name="decision" [(ngModel)]="decision">
                    <option value="APPROVED">Approve</option>
                    <option value="REJECTED">Reject</option>
                    <option value="RETURNED">Return to Agent</option>
                    <option value="ON_HOLD">Put On Hold</option>
                  </select>
                </div>
                <div class="field grow"><label>Remarks</label><input type="text" class="input" name="remarks" [(ngModel)]="remarks" placeholder="Reason / notes" /></div>
              </div>
              <div class="form-actions"><button type="submit" class="btn btn-primary">Submit Decision</button></div>
            </form>
          </div>
        </div>
      }

      <div class="panel">
        <div class="panel-head">Status Timeline</div>
        <div class="panel-body"><app-timeline [stages]="b.timeline" /></div>
      </div>

      <div class="grid-2">
        <div class="panel">
          <div class="panel-head">Claim Summary</div>
          <div class="panel-body">
            <dl class="kv">
              <dt>Policy</dt><dd>{{ b.claim.policyNo }}</dd>
              <dt>Estimated Loss</dt><dd>&#8377; {{ b.claim.estimatedLoss }}</dd>
              <dt>Agent</dt><dd>{{ b.claim.agentName }}</dd>
              <dt>Surveyor</dt><dd>{{ b.claim.surveyorName }}</dd>
            </dl>
            <p class="desc">{{ b.claim.description }}</p>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">Assessment</div>
          <div class="panel-body">
            @if (b.assessment; as a) {
              <dl class="kv">
                <dt>Gross Assessed</dt><dd>&#8377; {{ a.grossAssessed }}</dd>
                <dt>Net Payable</dt><dd class="big">&#8377; {{ a.netPayable }}</dd>
                <dt>Recommendation</dt><dd>{{ a.recommendation }}</dd>
                <dt>Surveyor</dt><dd>{{ a.surveyorName }}</dd>
              </dl>
            } @else {
              <p class="muted">No assessment submitted yet.</p>
            }
          </div>
        </div>
      </div>

      @if (b.approvals.length) {
        <div class="panel">
          <div class="panel-head">Approval Workflow</div>
          <div class="panel-body no-pad">
            <table class="table">
              <thead><tr><th>Level</th><th>Role</th><th>Approver</th><th>Decision</th><th>Remarks</th></tr></thead>
              <tbody>
                @for (a of b.approvals; track a.id) {
                  <tr>
                    <td>{{ a.level }}</td><td>{{ a.approverRole }}</td><td>{{ a.approverName }}</td>
                    <td><span class="pill" [class.pill-ok]="a.decision === 'APPROVED'" [class.pill-warn]="a.decision === 'PENDING'" [class.pill-danger]="a.decision !== 'APPROVED' && a.decision !== 'PENDING'">{{ a.decision }}</span></td>
                    <td>{{ a.remarks }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }

      @if (b.settlement; as st) {
        <div class="panel">
          <div class="panel-head">Settlement Override</div>
          <div class="panel-body">
            <dl class="kv">
              <dt>Current Amount</dt><dd class="big">&#8377; {{ st.finalAmount }}</dd>
              <dt>Status</dt><dd><span class="pill pill-info">{{ st.status }}</span></dd>
            </dl>
            <form (ngSubmit)="override(b.claim.id)">
              <div class="form-row">
                <div class="field"><label>Override Amount (&#8377;)</label><input type="number" step="0.01" min="0" class="input" name="amount" [(ngModel)]="overrideAmount" required /></div>
                <div class="field grow"><label>Justification</label><input type="text" class="input" name="justification" [(ngModel)]="overrideJustification" /></div>
              </div>
              <button type="submit" class="btn btn-light">Override Settlement</button>
            </form>
          </div>
        </div>
      }
    }
  `,
})
export class ManagerClaimDetailPage {
  private api = inject(ManagerApi);
  private flash = inject(FlashService);
  private confirm = inject(ConfirmService);

  id = input.required<string>();
  bundle = signal<AgentBundle | null>(null);
  decision = 'APPROVED';
  remarks = '';
  overrideAmount = '';
  overrideJustification = '';

  constructor() {
    queueMicrotask(() => this.reload());
  }
  private reload(): void {
    this.api.claim(Number(this.id())).subscribe((b) => this.bundle.set(b));
  }

  decide(id: number): void {
    this.api.decide(id, this.decision, this.remarks).subscribe(() => {
      this.flash.success('Decision recorded.');
      this.remarks = '';
      this.reload();
    });
  }

  async override(id: number): Promise<void> {
    if (!this.overrideAmount) return;
    if (!(await this.confirm.ask('Override the settlement amount?'))) return;
    this.api.override(id, this.overrideAmount, this.overrideJustification).subscribe(() => {
      this.flash.success('Settlement amount overridden.');
      this.overrideAmount = '';
      this.overrideJustification = '';
      this.reload();
    });
  }
}
