import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApi } from './admin.api';
import { NotificationTemplate } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';

/** Notification templates — ports admin/templates.jsp. */
@Component({
  selector: 'app-admin-templates',
  imports: [FormsModule],
  template: `
    <div class="page-head"><h1 class="page-title">Notification Templates</h1>
      <p class="page-sub">Message templates for SMS / email notifications</p></div>

    @for (t of rows(); track t.id) {
      <div class="panel">
        <div class="panel-head">{{ t.name }} <span class="pill pill-muted">{{ t.channel }}</span></div>
        <div class="panel-body">
          <div class="field">
            <label>Body</label>
            <textarea class="input" rows="2" [(ngModel)]="t.body"></textarea>
          </div>
          <label class="checkbox-line"><input type="checkbox" [(ngModel)]="t.activeBool" /> Active</label>
          <button class="btn btn-light btn-sm" (click)="save(t)">Save</button>
        </div>
      </div>
    }
  `,
})
export class AdminTemplatesPage {
  private api = inject(AdminApi);
  private flash = inject(FlashService);
  rows = signal<(NotificationTemplate & { activeBool: boolean })[]>([]);
  constructor() {
    this.api.templates().subscribe((r) =>
      this.rows.set(r.map((t) => ({ ...t, activeBool: !!t.active })))
    );
  }
  save(t: NotificationTemplate & { activeBool: boolean }): void {
    this.api.updateTemplate(t.id, t.activeBool, t.body ?? '').subscribe(() => this.flash.success('Template updated.'));
  }
}
