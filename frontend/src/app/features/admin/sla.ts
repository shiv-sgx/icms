import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApi } from './admin.api';
import { SlaConfig } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';

/** SLA config — ports admin/sla.jsp (inline hours edit). */
@Component({
  selector: 'app-admin-sla',
  imports: [FormsModule],
  template: `
    <div class="page-head"><h1 class="page-title">SLA Configuration</h1><p class="page-sub">Target hours per workflow stage</p></div>
    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Stage</th><th>Hours</th><th></th></tr></thead>
          <tbody>
            @for (s of rows(); track s.id) {
              <tr>
                <td>{{ s.stage }}</td>
                <td><span class="inline-form"><input type="number" min="1" class="input" [(ngModel)]="s.hours" /></span></td>
                <td><button class="btn btn-light btn-sm" (click)="save(s)">Save</button></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class AdminSlaPage {
  private api = inject(AdminApi);
  private flash = inject(FlashService);
  rows = signal<SlaConfig[]>([]);
  constructor() {
    this.api.sla().subscribe((r) => this.rows.set(r));
  }
  save(s: SlaConfig): void {
    this.api.updateSla(s.id, Number(s.hours)).subscribe(() => this.flash.success('SLA updated.'));
  }
}
