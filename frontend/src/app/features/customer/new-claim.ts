import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CustomerApi } from './customer.api';
import { Policy } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';

/** New claim submission — ports customer/new-claim.jsp. */
@Component({
  selector: 'app-customer-new-claim',
  imports: [ReactiveFormsModule],
  template: `
    <div class="page-head">
      <h1 class="page-title">New Claim Submission</h1>
      <p class="page-sub">Provide the incident details. The claim type follows your selected policy.</p>
    </div>

    @if (errors().length) {
      <div class="alert alert-error">
        @for (e of errors(); track e) { <div>{{ e }}</div> }
      </div>
    }

    @if (policies().length === 0) {
      <div class="alert alert-error">No active policies are linked to your account, so a claim cannot be filed.</div>
    } @else {
      <form class="form-card" [formGroup]="form">
        <div class="form-section">
          <h3>Policy &amp; Incident</h3>
          <div class="form-row">
            <div class="field">
              <label>Policy</label>
              <select class="input" formControlName="policyId">
                <option value="">— Select a policy —</option>
                @for (p of policies(); track p.id) {
                  <option [value]="p.id">{{ p.displayLabel }}</option>
                }
              </select>
            </div>
            <div class="field">
              <label>Claim Subtype</label>
              <input type="text" class="input" formControlName="claimSubtype" placeholder="e.g. Accident, Theft, Surgery" />
            </div>
          </div>
          <div class="form-row">
            <div class="field"><label>Incident Date</label><input type="date" class="input" formControlName="incidentDate" /></div>
            <div class="field"><label>Time of Incident</label><input type="time" class="input" formControlName="incidentTime" /></div>
            <div class="field"><label>Estimated Loss (&#8377;)</label><input type="number" step="0.01" min="0" class="input" formControlName="estimatedLoss" /></div>
          </div>
          <div class="form-row">
            <div class="field grow"><label>Location</label><input type="text" class="input" formControlName="incidentLocation" /></div>
            <div class="field"><label>City</label><input type="text" class="input" formControlName="city" /></div>
            <div class="field"><label>State</label><input type="text" class="input" formControlName="state" /></div>
            <div class="field"><label>PIN</label><input type="text" class="input" formControlName="pinCode" /></div>
          </div>
          <div class="field">
            <label>Description</label>
            <textarea class="input" rows="3" formControlName="description" placeholder="Describe what happened..."></textarea>
          </div>
        </div>

        <div class="form-section">
          <h3>Additional Details <span class="muted">(fill in what applies to your claim type)</span></h3>
          <div class="form-row">
            <div class="field"><label>Vehicle Registration No.</label><input type="text" class="input" formControlName="vehicleRegNo" /></div>
            <div class="field"><label>FIR Number</label><input type="text" class="input" formControlName="firNumber" /></div>
            <div class="field"><label>Police Station</label><input type="text" class="input" formControlName="policeStation" /></div>
          </div>
          <div class="form-row">
            <div class="field"><label>Hospital Name</label><input type="text" class="input" formControlName="hospitalName" /></div>
            <div class="field"><label>Workshop / Garage Name</label><input type="text" class="input" formControlName="workshopName" /></div>
            <div class="field"><label>Third Party</label><input type="text" class="input" formControlName="thirdParty" /></div>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-light" [disabled]="submitting()" (click)="save('draft')">Save as Draft</button>
          <button type="button" class="btn btn-primary" [disabled]="submitting()" (click)="save('submit')">Submit Claim</button>
        </div>
      </form>
    }
  `,
})
export class CustomerNewClaimPage {
  private fb = inject(FormBuilder);
  private api = inject(CustomerApi);
  private router = inject(Router);
  private flash = inject(FlashService);

  policies = signal<Policy[]>([]);
  errors = signal<string[]>([]);
  submitting = signal(false);

  form = this.fb.nonNullable.group({
    policyId: '',
    claimSubtype: '',
    incidentDate: '',
    incidentTime: '',
    estimatedLoss: '',
    incidentLocation: '',
    city: '',
    state: '',
    pinCode: '',
    description: '',
    vehicleRegNo: '',
    firNumber: '',
    policeStation: '',
    hospitalName: '',
    workshopName: '',
    thirdParty: '',
  });

  constructor() {
    this.api.policies().subscribe((p) => this.policies.set(p));
  }

  save(mode: 'draft' | 'submit'): void {
    this.errors.set([]);
    this.submitting.set(true);
    const body = { ...this.form.getRawValue(), mode };
    this.api.createClaim(body).subscribe({
      next: (res) => {
        this.flash.success(res.message);
        this.router.navigate(['/customer/claims', res.id]);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        const e = err?.error?.error;
        if (e?.fields) this.errors.set(Object.values(e.fields));
        else this.errors.set([e?.message || 'Could not save the claim.']);
      },
    });
  }
}
