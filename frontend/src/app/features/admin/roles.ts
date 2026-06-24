import { Component, inject, signal } from '@angular/core';
import { AdminApi } from './admin.api';
import { RoleWithCount } from '../../shared/models';

/** Role management — ports admin/roles.jsp (read-only with user counts). */
@Component({
  selector: 'app-admin-roles',
  imports: [],
  template: `
    <div class="page-head"><h1 class="page-title">Role &amp; Permission Management</h1>
      <p class="page-sub">System roles and their user counts</p></div>
    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Role</th><th>Description</th><th>Users</th></tr></thead>
          <tbody>
            @for (r of roles(); track r.id) {
              <tr><td><span class="pill pill-info">{{ r.name }}</span></td><td>{{ r.description }}</td><td>{{ r.userCount }}</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class AdminRolesPage {
  private api = inject(AdminApi);
  roles = signal<RoleWithCount[]>([]);
  constructor() {
    this.api.roles().subscribe((r) => this.roles.set(r));
  }
}
