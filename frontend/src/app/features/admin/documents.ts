import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminApi } from './admin.api';
import { DocumentRequirement } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';
import { ConfirmService } from '../../core/services/confirm.service';

const TYPES = ['MOTOR', 'HEALTH', 'PROPERTY', 'LIFE', 'TRAVEL', 'LIABILITY'];

/** Claim config / document requirements — ports admin/documents.jsp. */
@Component({
  selector: 'app-admin-documents',
  imports: [FormsModule],
  template: `
    <div class="page-head"><h1 class="page-title">Claim Configuration</h1>
      <p class="page-sub">Document requirements by claim type</p></div>

    <div class="panel">
      <div class="panel-head">Add Requirement</div>
      <div class="panel-body">
        <form class="form-card" (ngSubmit)="add()">
          <div class="form-row">
            <div class="field"><label>Claim Type</label>
              <select class="input" name="claimType" [(ngModel)]="nd.claimType" required>
                @for (t of types; track t) { <option [value]="t">{{ t }}</option> }
              </select>
            </div>
            <div class="field"><label>Subtype (optional)</label><input type="text" class="input" name="claimSubtype" [(ngModel)]="nd.claimSubtype" /></div>
            <div class="field"><label>Document Type</label><input type="text" class="input" name="docType" [(ngModel)]="nd.docType" required /></div>
            <div class="field"><label>Required</label>
              <label class="checkbox-line"><input type="checkbox" name="required" [(ngModel)]="nd.required" /> Required</label>
            </div>
          </div>
          <div class="form-actions"><button type="submit" class="btn btn-primary">Add</button></div>
        </form>
      </div>
    </div>

    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Claim Type</th><th>Subtype</th><th>Document</th><th>Required</th><th></th></tr></thead>
          <tbody>
            @for (d of rows(); track d.id) {
              <tr>
                <td>{{ d.claimType }}</td>
                <td>{{ d.claimSubtype || '— any —' }}</td>
                <td>{{ d.docType }}</td>
                <td>@if (d.required) { <span class="pill pill-ok">Required</span> } @else { <span class="pill pill-muted">Optional</span> }</td>
                <td><button class="btn btn-light btn-sm" (click)="remove(d)">Delete</button></td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="empty">No requirements configured.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class AdminDocumentsPage {
  private api = inject(AdminApi);
  private flash = inject(FlashService);
  private confirm = inject(ConfirmService);
  types = TYPES;
  rows = signal<DocumentRequirement[]>([]);
  nd = { claimType: 'MOTOR', claimSubtype: '', docType: '', required: true };

  constructor() {
    this.reload();
  }
  private reload(): void {
    this.api.documents().subscribe((r) => this.rows.set(r));
  }
  add(): void {
    this.api.addDocument({ ...this.nd }).subscribe({
      next: () => {
        this.flash.success('Document requirement added.');
        this.nd = { claimType: 'MOTOR', claimSubtype: '', docType: '', required: true };
        this.reload();
      },
      error: (e: HttpErrorResponse) => this.flash.error(e?.error?.error?.message || 'Could not add requirement.'),
    });
  }
  async remove(d: DocumentRequirement): Promise<void> {
    if (!(await this.confirm.ask('Remove this requirement?'))) return;
    this.api.deleteDocument(d.id).subscribe(() => {
      this.flash.success('Document requirement removed.');
      this.reload();
    });
  }
}
