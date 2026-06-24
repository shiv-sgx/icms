import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApi } from './admin.api';
import { ApprovalThreshold } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';

/** Approval thresholds — ports admin/thresholds.jsp. */
@Component({
  selector: 'app-admin-thresholds',
  imports: [FormsModule],
  template: `
    <div class="page-head"><h1 class="page-title">Approval Amount Thresholds</h1>
      <p class="page-sub">Amount bands that determine the required approval level</p></div>
    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Level</th><th>Label</th><th>Min / Max (&#8377;)</th></tr></thead>
          <tbody>
            @for (t of rows(); track t.id) {
              <tr>
                <td><span class="pill pill-info">{{ t.level }}</span></td>
                <td>{{ t.label }}</td>
                <td>
                  <span class="inline-form">
                    <input type="number" step="0.01" class="input" [(ngModel)]="t.minAmount" />
                    <input type="number" step="0.01" class="input" [(ngModel)]="t.maxAmount" placeholder="(no limit)" />
                    <button class="btn btn-light btn-sm" (click)="save(t)">Save</button>
                  </span>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class AdminThresholdsPage {
  private api = inject(AdminApi);
  private flash = inject(FlashService);
  rows = signal<ApprovalThreshold[]>([]);
  constructor() {
    this.api.thresholds().subscribe((r) => this.rows.set(r));
  }
  save(t: ApprovalThreshold): void {
    this.api.updateThreshold(t.id, String(t.minAmount), t.maxAmount == null ? '' : String(t.maxAmount)).subscribe(() =>
      this.flash.success('Threshold updated.')
    );
  }
}
