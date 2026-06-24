import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerApi } from './customer.api';
import { ClaimBundle } from '../../shared/models';
import { AuthService } from '../../core/auth/auth.service';
import { FlashService } from '../../core/services/flash.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { StatusPill } from '../../shared/components/status-pill';
import { Timeline } from '../../shared/components/timeline';
import { MessageThread } from '../../shared/components/message-thread';

/** Customer claim detail — ports customer/claim-detail.jsp. */
@Component({
  selector: 'app-customer-claim-detail',
  imports: [RouterLink, FormsModule, StatusPill, Timeline, MessageThread],
  template: `
    @if (bundle(); as b) {
      <div class="page-head with-action">
        <div>
          <h1 class="page-title">
            {{ b.claim.claimNo }}
            <app-status-pill [label]="b.claim.statusLabel" [pillClass]="b.claim.statusPill" />
          </h1>
          <p class="page-sub">
            {{ b.claim.claimType }}@if (b.claim.claimSubtype) { · {{ b.claim.claimSubtype }} } &middot; Policy {{ b.claim.policyNo }}
          </p>
        </div>
        <a routerLink="/customer/claims" class="btn btn-light">&laquo; Back to claims</a>
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
              <dt>Claimant</dt><dd>{{ b.claim.claimantName }}</dd>
              <dt>Incident Date</dt><dd>{{ b.claim.incidentDate }} {{ b.claim.incidentTime }}</dd>
              <dt>Location</dt><dd>{{ b.claim.incidentLocation }} {{ b.claim.city }} {{ b.claim.state }}</dd>
              <dt>Estimated Loss</dt><dd>&#8377; {{ b.claim.estimatedLoss }}</dd>
              @if (b.claim.vehicleRegNo) { <dt>Vehicle No.</dt><dd>{{ b.claim.vehicleRegNo }}</dd> }
              @if (b.claim.firNumber) { <dt>FIR Number</dt><dd>{{ b.claim.firNumber }}</dd> }
              @if (b.claim.hospitalName) { <dt>Hospital</dt><dd>{{ b.claim.hospitalName }}</dd> }
              <dt>Agent</dt><dd>{{ b.claim.agentName || 'Not yet assigned' }}</dd>
              <dt>Filed</dt><dd>{{ b.claim.filedAt }}</dd>
            </dl>
            <p class="desc">{{ b.claim.description }}</p>

            @if (b.claim.withdrawable) {
              <button class="btn btn-danger" (click)="withdraw(b.claim.id)">Withdraw Claim</button>
            }
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
                  <tr><td colspan="3" class="empty">No documents required.</td></tr>
                }
              </tbody>
            </table>
          </div>
          <div class="panel-foot">
            <p class="muted small">Document upload will be available in an upcoming update.</p>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-head">Communication Center</div>
        <div class="panel-body">
          <app-message-thread [messages]="b.messages" [currentUserId]="currentUserId()" />
          <form class="msg-form" (ngSubmit)="send(b.claim.id)">
            <input type="text" class="input" name="content" [(ngModel)]="draftMessage" placeholder="Type your message..." required />
            <button type="submit" class="btn btn-primary" [disabled]="!draftMessage().trim()">Send</button>
          </form>
        </div>
      </div>
    }
  `,
})
export class CustomerClaimDetailPage {
  private api = inject(CustomerApi);
  private auth = inject(AuthService);
  private flash = inject(FlashService);
  private confirm = inject(ConfirmService);

  /** Route param (withComponentInputBinding). */
  id = input.required<string>();
  bundle = signal<ClaimBundle | null>(null);
  draftMessage = signal('');
  currentUserId = computed(() => this.auth.user()?.id ?? null);

  constructor() {
    // Load whenever the route id changes.
    queueMicrotask(() => this.reload());
  }

  private reload(): void {
    this.api.claim(Number(this.id())).subscribe((b) => this.bundle.set(b));
  }

  send(claimId: number): void {
    const content = this.draftMessage().trim();
    if (!content) return;
    this.api.postMessage(claimId, content).subscribe(() => {
      this.draftMessage.set('');
      this.reload();
    });
  }

  async withdraw(claimId: number): Promise<void> {
    const ok = await this.confirm.ask('Withdraw this claim? This cannot be undone.');
    if (!ok) return;
    this.api.withdraw(claimId).subscribe(() => {
      this.flash.success('Claim withdrawn.');
      this.reload();
    });
  }
}
