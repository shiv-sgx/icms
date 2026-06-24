import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AgentApi } from './agent.api';
import { AgentBundle } from '../../shared/models';
import { AuthService } from '../../core/auth/auth.service';
import { FlashService } from '../../core/services/flash.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { StatusPill } from '../../shared/components/status-pill';
import { Timeline } from '../../shared/components/timeline';
import { MessageThread } from '../../shared/components/message-thread';

/** Agent claim detail — ports agent/claim-detail.jsp (action bar + full aggregation). */
@Component({
  selector: 'app-agent-claim-detail',
  imports: [RouterLink, FormsModule, StatusPill, Timeline, MessageThread],
  template: `
    @if (bundle(); as b) {
      <div class="page-head with-action">
        <div>
          <h1 class="page-title">
            {{ b.claim.claimNo }}
            <app-status-pill [label]="b.claim.statusLabel" [pillClass]="b.claim.statusPill" />
            <span class="pill risk-{{ b.claim.riskLevel }}">{{ b.claim.riskLevel }} RISK</span>
          </h1>
          <p class="page-sub">
            {{ b.claim.claimType }}@if (b.claim.claimSubtype) { · {{ b.claim.claimSubtype }} }
            &middot; {{ b.claim.claimantName }} &middot; Policy {{ b.claim.policyNo }}
          </p>
        </div>
        <a routerLink="/agent/claims" class="btn btn-light">&laquo; Back</a>
      </div>

      <!-- Action bar -->
      <div class="action-bar">
        @if (b.claim.status === 'SUBMITTED') {
          <button class="btn btn-primary" (click)="acknowledge(b.claim.id)">Acknowledge</button>
        }
        @if (canAssign(b.claim.status)) {
          <span class="inline-form">
            <select class="input" [(ngModel)]="surveyorId">
              <option [ngValue]="0">Assign surveyor…</option>
              @for (s of b.surveyors ?? []; track s.id) {
                <option [ngValue]="s.id">{{ s.fullName }} ({{ s.branch }})</option>
              }
            </select>
            <button class="btn btn-primary" (click)="assign(b.claim.id)">Assign</button>
          </span>
        }
        @if (canForward(b.claim.status)) {
          <button class="btn btn-primary" (click)="forward(b.claim.id)">Forward for Approval</button>
        }
        @if (canSettle(b.claim.status)) {
          <a class="btn btn-primary" [routerLink]="['/agent/claims', b.claim.id, 'settlement']">Settlement</a>
        }
      </div>

      <div class="panel">
        <div class="panel-head">Claim Status Timeline</div>
        <div class="panel-body"><app-timeline [stages]="b.timeline" /></div>
      </div>

      <div class="grid-2">
        <div class="panel">
          <div class="panel-head">Claim Summary</div>
          <div class="panel-body">
            <dl class="kv">
              <dt>Incident</dt><dd>{{ b.claim.incidentDate }} {{ b.claim.incidentTime }}</dd>
              <dt>Location</dt><dd>{{ b.claim.incidentLocation }} {{ b.claim.city }}</dd>
              <dt>Estimated Loss</dt><dd>&#8377; {{ b.claim.estimatedLoss }}</dd>
              <dt>Fraud Score</dt><dd>{{ b.claim.fraudScore }} / 100</dd>
              <dt>Surveyor</dt><dd>{{ b.claim.surveyorName || 'Not assigned' }}</dd>
            </dl>
            <p class="desc">{{ b.claim.description }}</p>
            <label class="lbl">Internal Notes</label>
            <textarea class="input" rows="2" [(ngModel)]="notes"></textarea>
            <button class="btn btn-light btn-sm" (click)="saveNote(b.claim.id)">Save Notes</button>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">Documents</div>
          <div class="panel-body no-pad">
            <table class="table">
              <thead><tr><th>Document</th><th>Status</th><th>Verification</th></tr></thead>
              <tbody>
                @for (d of b.documents; track d.id) {
                  <tr>
                    <td>{{ d.docType }}@if (d.fileName) { <br /><span class="muted small">{{ d.fileName }}</span> }</td>
                    <td><span class="pill" [class.pill-ok]="d.uploadStatus === 'UPLOADED'" [class.pill-warn]="d.uploadStatus !== 'UPLOADED'">{{ d.uploadStatus }}</span></td>
                    <td><span class="pill pill-muted">{{ d.verificationStatus }}</span></td>
                  </tr>
                } @empty {
                  <tr><td colspan="3" class="empty">No documents.</td></tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>

      @if (b.assessment) {
        <div class="panel">
          <div class="panel-head">Assessment Report <span class="muted">by {{ b.assessment.surveyorName }}</span></div>
          <div class="panel-body">
            <div class="card-grid">
              <div class="stat-card"><div class="stat-label">Gross Assessed</div><div class="stat-value">&#8377; {{ b.assessment.grossAssessed }}</div></div>
              <div class="stat-card"><div class="stat-label">Deductible</div><div class="stat-value">&#8377; {{ b.assessment.policyDeductible }}</div></div>
              <div class="stat-card"><div class="stat-label">Net Payable</div><div class="stat-value">&#8377; {{ b.assessment.netPayable }}</div></div>
              <div class="stat-card"><div class="stat-label">Recommendation</div><div class="stat-value small">{{ b.assessment.recommendation }}</div></div>
            </div>
            @if (b.components.length) {
              <table class="table">
                <thead><tr><th>Component</th><th>Severity</th><th>Repair Cost</th><th>Replace?</th></tr></thead>
                <tbody>
                  @for (c of b.components; track c.id) {
                    <tr><td>{{ c.component }}</td><td>{{ c.severity }}</td><td>&#8377; {{ c.repairCost }}</td><td>{{ c.replaceFlag ? 'Yes' : 'No' }}</td></tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </div>
      }

      @if (b.approvals.length) {
        <div class="panel">
          <div class="panel-head">Approval Workflow</div>
          <div class="panel-body no-pad">
            <table class="table">
              <thead><tr><th>Level</th><th>Role</th><th>Approver</th><th>Decision</th><th>Remarks</th></tr></thead>
              <tbody>
                @for (a of b.approvals; track a.id) {
                  <tr>
                    <td>{{ a.level }}</td>
                    <td>{{ a.approverRole }}</td>
                    <td>{{ a.approverName }}</td>
                    <td><span class="pill" [class.pill-ok]="a.decision === 'APPROVED'" [class.pill-warn]="a.decision === 'PENDING'" [class.pill-danger]="a.decision !== 'APPROVED' && a.decision !== 'PENDING'">{{ a.decision }}</span></td>
                    <td>{{ a.remarks }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      }

      <div class="panel">
        <div class="panel-head">Communication Center</div>
        <div class="panel-body">
          <app-message-thread [messages]="b.messages" [currentUserId]="currentUserId()" />
          <form class="msg-form" (ngSubmit)="send(b.claim.id)">
            <input type="text" class="input" name="content" [(ngModel)]="draftMessage" placeholder="Message the claimant..." required />
            <button type="submit" class="btn btn-primary" [disabled]="!draftMessage().trim()">Send</button>
          </form>
        </div>
      </div>
    }
  `,
})
export class AgentClaimDetailPage {
  private api = inject(AgentApi);
  private auth = inject(AuthService);
  private flash = inject(FlashService);
  private confirm = inject(ConfirmService);

  id = input.required<string>();
  bundle = signal<AgentBundle | null>(null);
  notes = '';
  surveyorId = 0;
  draftMessage = signal('');
  currentUserId = computed(() => this.auth.user()?.id ?? null);

  constructor() {
    queueMicrotask(() => this.reload());
  }

  private reload(): void {
    this.api.claim(Number(this.id())).subscribe((b) => {
      this.bundle.set(b);
      this.notes = b.claim.internalNotes ?? '';
      this.surveyorId = b.claim.surveyorId ? Number(b.claim.surveyorId) : 0;
    });
  }

  canAssign(s: string): boolean {
    return ['SUBMITTED', 'UNDER_REVIEW', 'SURVEY_SCHEDULED', 'UNDER_ASSESSMENT'].includes(s);
  }
  canForward(s: string): boolean {
    return ['UNDER_REVIEW', 'UNDER_ASSESSMENT', 'SURVEY_SCHEDULED', 'ON_HOLD'].includes(s);
  }
  canSettle(s: string): boolean {
    return ['APPROVED', 'SETTLEMENT_PROCESSING', 'SETTLED', 'CLOSED'].includes(s);
  }

  acknowledge(id: number): void {
    this.api.acknowledge(id).subscribe(() => {
      this.flash.success('Claim acknowledged and under review.');
      this.reload();
    });
  }
  assign(id: number): void {
    if (!this.surveyorId) {
      this.flash.error('Please choose a surveyor.');
      return;
    }
    this.api.assignSurveyor(id, this.surveyorId).subscribe(() => {
      this.flash.success('Surveyor assigned. The claim is now scheduled for survey.');
      this.reload();
    });
  }
  async forward(id: number): Promise<void> {
    if (!(await this.confirm.ask('Forward this claim for approval?'))) return;
    this.api.forward(id).subscribe(() => {
      this.flash.success('Claim forwarded for approval.');
      this.reload();
    });
  }
  saveNote(id: number): void {
    this.api.saveNote(id, this.notes).subscribe(() => this.flash.success('Internal notes saved.'));
  }
  send(id: number): void {
    const content = this.draftMessage().trim();
    if (!content) return;
    this.api.postMessage(id, content).subscribe(() => {
      this.draftMessage.set('');
      this.reload();
    });
  }
}
