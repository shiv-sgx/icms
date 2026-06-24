import { Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormArray, FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { SurveyorApi } from './surveyor.api';
import { AssessScreen } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';
import { ConfirmService } from '../../core/services/confirm.service';
import { StatusPill } from '../../shared/components/status-pill';

const fmt = (n: number) => (Number.isFinite(n) ? n : 0).toFixed(2);

/** Surveyor assessment — ports surveyor/assess.jsp incl. the live net-payable calc. */
@Component({
  selector: 'app-surveyor-assess',
  imports: [RouterLink, ReactiveFormsModule, FormsModule, StatusPill],
  template: `
    @if (screen(); as s) {
      <div class="page-head with-action">
        <div>
          <h1 class="page-title">Claim Assessment — {{ s.claim.claimNo }}</h1>
          <p class="page-sub">
            {{ s.claim.claimType }}@if (s.claim.claimSubtype) { · {{ s.claim.claimSubtype }} }
            &middot; {{ s.claim.claimantName }} &middot;
            <app-status-pill [label]="s.claim.statusLabel" [pillClass]="s.claim.statusPill" />
          </p>
        </div>
        <a routerLink="/surveyor/dashboard" class="btn btn-light">&laquo; Back</a>
      </div>

      <div class="panel">
        <div class="panel-head">Claim Summary</div>
        <div class="panel-body">
          <dl class="kv">
            <dt>Incident</dt><dd>{{ s.claim.incidentDate }} {{ s.claim.incidentTime }}</dd>
            <dt>Location</dt><dd>{{ s.claim.incidentLocation }} {{ s.claim.city }} {{ s.claim.state }}</dd>
            <dt>Estimated Loss</dt><dd>&#8377; {{ s.claim.estimatedLoss }}</dd>
          </dl>
          <p class="desc">{{ s.claim.description }}</p>
        </div>
      </div>

      @if (s.assessment && s.assessment.status === 'SUBMITTED') {
        <!-- Read-only submitted assessment -->
        <div class="panel">
          <div class="panel-head">Submitted Assessment <span class="muted">Ref: {{ s.assessment.reportRefNo }}</span></div>
          <div class="panel-body">
            <div class="card-grid">
              <div class="stat-card"><div class="stat-label">Gross Assessed</div><div class="stat-value">&#8377; {{ s.assessment.grossAssessed }}</div></div>
              <div class="stat-card"><div class="stat-label">Depreciation</div><div class="stat-value">&#8377; {{ s.assessment.depreciationAmt }}</div></div>
              <div class="stat-card"><div class="stat-label">Salvage</div><div class="stat-value">&#8377; {{ s.assessment.salvageValue }}</div></div>
              <div class="stat-card"><div class="stat-label">Net Payable</div><div class="stat-value">&#8377; {{ s.assessment.netPayable }}</div></div>
            </div>
            <p><strong>Recommendation:</strong> {{ s.assessment.recommendation }}</p>
            <p class="muted">{{ s.assessment.siteObservations }}</p>
            @if (s.components.length) {
              <table class="table">
                <thead><tr><th>Component</th><th>Severity</th><th>Repair Cost</th><th>Replace?</th></tr></thead>
                <tbody>
                  @for (c of s.components; track c.id) {
                    <tr><td>{{ c.component }}</td><td>{{ c.severity }}</td><td>&#8377; {{ c.repairCost }}</td><td>{{ c.replaceFlag ? 'Yes' : 'No' }}</td></tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </div>
      } @else {
        <!-- Assessment form -->
        <form class="form-card" [formGroup]="form">
          <div class="form-section">
            <h3>Site Visit</h3>
            <div class="form-row">
              <div class="field"><label>Visit Date</label><input type="date" class="input" formControlName="visitDate" /></div>
              <div class="field"><label>Visit Time</label><input type="time" class="input" formControlName="visitTime" /></div>
              <div class="field"><label>Report Reference No.</label><input type="text" class="input" formControlName="reportRefNo" placeholder="SRV-2026-..." /></div>
            </div>
            <div class="field"><label>Site Observations</label><textarea class="input" rows="2" formControlName="siteObservations"></textarea></div>
          </div>

          <div class="form-section">
            <h3>Component Breakdown</h3>
            <table class="table comp-table">
              <thead><tr><th>Component</th><th>Severity</th><th>Repair Cost (&#8377;)</th><th>Replace?</th><th></th></tr></thead>
              <tbody formArrayName="components">
                @for (row of components.controls; track $index) {
                  <tr [formGroupName]="$index">
                    <td><input type="text" class="input" formControlName="component" placeholder="e.g. Front Bumper" /></td>
                    <td><select class="input" formControlName="severity"><option>MINOR</option><option>MODERATE</option><option>SEVERE</option></select></td>
                    <td><input type="number" step="0.01" min="0" class="input" formControlName="repairCost" /></td>
                    <td><select class="input" formControlName="replaceFlag"><option [ngValue]="false">No</option><option [ngValue]="true">Yes</option></select></td>
                    <td><button type="button" class="btn btn-light btn-sm" (click)="removeComponent($index)">&times;</button></td>
                  </tr>
                }
              </tbody>
            </table>
            <button type="button" class="btn btn-light btn-sm" (click)="addComponent()">+ Add Component</button>
          </div>

          <div class="form-section">
            <h3>Settlement Calculation</h3>
            <div class="form-row">
              <div class="field"><label>Policy Deductible (&#8377;)</label><input type="number" step="0.01" min="0" class="input" formControlName="policyDeductible" /></div>
              <div class="field"><label>Depreciation (%)</label><input type="number" step="0.01" min="0" class="input" formControlName="depreciationPct" /></div>
              <div class="field"><label>Salvage Value (&#8377;)</label><input type="number" step="0.01" min="0" class="input" formControlName="salvageValue" /></div>
            </div>
            <div class="card-grid calc-out">
              <div class="stat-card"><div class="stat-label">Gross Assessed</div><div class="stat-value">&#8377; {{ calc().gross }}</div></div>
              <div class="stat-card"><div class="stat-label">Depreciation Amt</div><div class="stat-value">&#8377; {{ calc().depr }}</div></div>
              <div class="stat-card"><div class="stat-label">Net Payable</div><div class="stat-value">&#8377; {{ calc().net }}</div></div>
            </div>
          </div>

          <div class="form-section">
            <h3>Recommendation</h3>
            <div class="form-row">
              <div class="field">
                <label>Recommendation</label>
                <select class="input" formControlName="recommendation">
                  <option value="APPROVE_FULL">Approve Full</option>
                  <option value="PARTIAL_APPROVE">Partial Approve</option>
                  <option value="REJECT">Reject</option>
                </select>
              </div>
              <div class="field grow"><label>Remarks</label><input type="text" class="input" formControlName="remarks" /></div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="btn btn-primary" [disabled]="submitting()" (click)="submit(s.claim.id)">Submit Assessment</button>
          </div>
        </form>
      }

      <!-- Survey documents + upload -->
      <div class="panel">
        <div class="panel-head">Survey Documents</div>
        <div class="panel-body no-pad">
          <table class="table">
            <thead><tr><th>Document</th><th>Status</th></tr></thead>
            <tbody>
              @for (d of s.documents; track d.id) {
                <tr>
                  <td>{{ d.docType }}@if (d.fileName) { — <span class="muted small">{{ d.fileName }}</span> }</td>
                  <td><span class="pill" [class.pill-ok]="d.uploadStatus === 'UPLOADED'" [class.pill-warn]="d.uploadStatus !== 'UPLOADED'">{{ d.uploadStatus }}</span></td>
                </tr>
              } @empty {
                <tr><td colspan="2" class="empty">No documents.</td></tr>
              }
            </tbody>
          </table>
        </div>
        <div class="panel-foot">
          <form class="upload-form" (ngSubmit)="upload(s.claim.id)">
            <input type="text" class="input" name="rdt" [(ngModel)]="reportDocType" placeholder="e.g. Survey Report, Site Photo" required />
            <input type="file" class="input" (change)="onFile($event)" required />
            <button type="submit" class="btn btn-primary" [disabled]="!reportFile">Upload</button>
          </form>
        </div>
      </div>
    }
  `,
})
export class SurveyorAssessPage {
  private fb = inject(FormBuilder);
  private api = inject(SurveyorApi);
  private flash = inject(FlashService);
  private confirm = inject(ConfirmService);

  id = input.required<string>();
  screen = signal<AssessScreen | null>(null);
  submitting = signal(false);
  reportDocType = '';
  reportFile: File | null = null;

  form = this.fb.nonNullable.group({
    visitDate: '',
    visitTime: '',
    reportRefNo: '',
    siteObservations: '',
    components: this.fb.array([this.newComponent()]),
    policyDeductible: '0',
    depreciationPct: '0',
    salvageValue: '0',
    recommendation: 'PARTIAL_APPROVE',
    remarks: '',
  });

  /** Live recompute driven by form value changes (replaces icms.js recalc()). */
  private formValue = toSignal(this.form.valueChanges, { initialValue: this.form.getRawValue() });
  calc = computed(() => {
    this.formValue(); // dependency
    const v = this.form.getRawValue();
    const gross = (v.components ?? []).reduce((sum, c) => sum + (Number(c.repairCost) || 0), 0);
    const depr = (gross * (Number(v.depreciationPct) || 0)) / 100;
    const net = Math.max(0, gross - (Number(v.policyDeductible) || 0) - depr - (Number(v.salvageValue) || 0));
    return { gross: fmt(gross), depr: fmt(depr), net: fmt(net) };
  });

  get components(): FormArray {
    return this.form.get('components') as FormArray;
  }

  private newComponent() {
    return this.fb.nonNullable.group({
      component: '',
      severity: 'MODERATE',
      repairCost: '0',
      replaceFlag: false,
    });
  }

  constructor() {
    queueMicrotask(() => this.reload());
  }

  private reload(): void {
    this.api.assessment(Number(this.id())).subscribe((s) => this.screen.set(s));
  }

  addComponent(): void {
    this.components.push(this.newComponent());
  }
  removeComponent(i: number): void {
    if (this.components.length > 1) this.components.removeAt(i);
  }

  async submit(claimId: number): Promise<void> {
    if (!(await this.confirm.ask('Submit this assessment? It cannot be edited afterwards.'))) return;
    this.submitting.set(true);
    const body = { ...this.form.getRawValue() };
    this.api.submitAssessment(claimId, body).subscribe({
      next: (res) => {
        this.flash.success(res.data.message);
        this.submitting.set(false);
        this.reload();
      },
      error: (err) => {
        this.submitting.set(false);
        this.flash.error(err?.error?.error?.message || 'Could not submit the assessment.');
      },
    });
  }

  onFile(e: Event): void {
    this.reportFile = (e.target as HTMLInputElement).files?.[0] ?? null;
  }

  upload(claimId: number): void {
    if (!this.reportFile || !this.reportDocType.trim()) return;
    this.api.uploadReport(claimId, this.reportDocType.trim(), this.reportFile).subscribe({
      next: () => {
        this.flash.success('Document uploaded.');
        this.reportDocType = '';
        this.reportFile = null;
        this.reload();
      },
      error: (err) => this.flash.error(err?.error?.error?.message || 'Upload failed.'),
    });
  }
}
