import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AdminApi, UsersResponse } from './admin.api';
import { AdminUser } from '../../shared/models';
import { FlashService } from '../../core/services/flash.service';
import { Paginator } from '../../shared/components/paginator';

/** User management — ports admin/users.jsp (create + search + inline update/reset). */
@Component({
  selector: 'app-admin-users',
  imports: [FormsModule, Paginator],
  template: `
    <div class="page-head"><h1 class="page-title">User Management</h1></div>

    @if (data(); as d) {
      <!-- Create user -->
      <div class="panel">
        <div class="panel-head">Create User</div>
        <div class="panel-body">
          <form class="form-card" (ngSubmit)="create()">
            <div class="form-row">
              <div class="field"><label>Full Name</label><input type="text" class="input" name="fullName" [(ngModel)]="nu.fullName" required /></div>
              <div class="field"><label>Email</label><input type="email" class="input" name="email" [(ngModel)]="nu.email" required /></div>
              <div class="field"><label>Username</label><input type="text" class="input" name="username" [(ngModel)]="nu.username" required /></div>
            </div>
            <div class="form-row">
              <div class="field"><label>Password</label><input type="text" class="input" name="password" [(ngModel)]="nu.password" placeholder="min 6 chars" required /></div>
              <div class="field">
                <label>Role</label>
                <select class="input" name="roleId" [(ngModel)]="nu.roleId" required>
                  <option [ngValue]="0">— role —</option>
                  @for (r of d.roles; track r.id) { <option [ngValue]="r.id">{{ r.name }}</option> }
                </select>
              </div>
              <div class="field"><label>Branch</label><input type="text" class="input" name="branch" [(ngModel)]="nu.branch" /></div>
            </div>
            <div class="form-actions"><button type="submit" class="btn btn-primary">Create User</button></div>
          </form>
        </div>
      </div>

      <!-- Search + list -->
      <div class="filter-bar">
        <input type="text" class="input" placeholder="Search by name, email, username..." [(ngModel)]="q" />
        <select class="input" [(ngModel)]="role">
          <option value="">All roles</option>
          @for (r of d.roles; track r.id) { <option [value]="r.name">{{ r.name }}</option> }
        </select>
        <button class="btn btn-primary" (click)="load(1)">Search</button>
        <button class="btn btn-light" (click)="reset()">Reset</button>
      </div>

      <div class="panel">
        <div class="panel-body no-pad">
          <table class="table">
            <thead><tr><th>User</th><th>Role / Status</th><th>Last Login</th><th>Update</th><th>Reset Password</th></tr></thead>
            <tbody>
              @for (u of d.users.items; track u.id) {
                <tr>
                  <td><strong>{{ u.fullName }}</strong><br /><span class="muted small">{{ u.username }} · {{ u.email }}</span></td>
                  <td><span class="pill pill-info">{{ u.roleName }}</span>
                    <span class="pill" [class.pill-ok]="u.status === 'ACTIVE'" [class.pill-muted]="u.status !== 'ACTIVE'">{{ u.status }}</span></td>
                  <td class="muted small">{{ u.lastLogin }}</td>
                  <td>
                    <span class="inline-form">
                      <select class="input" [(ngModel)]="edit[u.id].status">
                        <option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option>
                      </select>
                      <select class="input" [(ngModel)]="edit[u.id].roleId">
                        @for (r of d.roles; track r.id) { <option [ngValue]="r.id">{{ r.name }}</option> }
                      </select>
                      <button class="btn btn-light btn-sm" (click)="update(u)">Save</button>
                    </span>
                  </td>
                  <td>
                    <span class="inline-form">
                      <input type="text" class="input" placeholder="new password" [(ngModel)]="edit[u.id].pw" />
                      <button class="btn btn-light btn-sm" (click)="resetPw(u)">Reset</button>
                    </span>
                  </td>
                </tr>
              } @empty {
                <tr><td colspan="5" class="empty">No users found.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </div>

      <app-paginator [page]="d.users.page" [size]="d.users.size" [total]="d.users.total" (pageChange)="load($event)" />
    }
  `,
})
export class AdminUsersPage {
  private api = inject(AdminApi);
  private flash = inject(FlashService);

  data = signal<UsersResponse | null>(null);
  q = '';
  role = '';
  nu = { fullName: '', email: '', username: '', password: '', roleId: 0, branch: '' };
  edit: Record<number, { status: string; roleId: number; pw: string }> = {};

  constructor() {
    this.load(1);
  }

  load(p: number): void {
    this.api.users(this.q, this.role, p, 15).subscribe((d) => {
      this.data.set(d);
      this.edit = {};
      for (const u of d.users.items) this.edit[u.id] = { status: u.status, roleId: u.roleId, pw: '' };
    });
  }

  reset(): void {
    this.q = '';
    this.role = '';
    this.load(1);
  }

  create(): void {
    this.api.createUser({ ...this.nu }).subscribe({
      next: () => {
        this.flash.success('User created.');
        this.nu = { fullName: '', email: '', username: '', password: '', roleId: 0, branch: '' };
        this.load(1);
      },
      error: (e: HttpErrorResponse) => this.flash.error(e?.error?.error?.message || 'Could not create user.'),
    });
  }

  update(u: AdminUser): void {
    const e = this.edit[u.id];
    this.api.updateUser(u.id, e.status, e.roleId).subscribe(() => {
      this.flash.success('User updated.');
      this.load(this.data()!.users.page);
    });
  }

  resetPw(u: AdminUser): void {
    const pw = this.edit[u.id].pw;
    this.api.resetPassword(u.id, pw).subscribe({
      next: () => {
        this.flash.success('Password reset.');
        this.edit[u.id].pw = '';
      },
      error: (e: HttpErrorResponse) => this.flash.error(e?.error?.error?.message || 'Could not reset password.'),
    });
  }
}
