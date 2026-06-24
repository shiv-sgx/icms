import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { FlashService } from '../services/flash.service';
import { Role } from '../../shared/models';

interface NavItem {
  label: string;
  link: string;
}

/** Role-based sidebar navigation — mirrors the role blocks in layout.jsp. */
const NAV: Record<Role, NavItem[]> = {
  CUSTOMER: [
    { label: 'Dashboard', link: '/customer/dashboard' },
    { label: 'My Claims', link: '/customer/claims' },
    { label: 'New Claim', link: '/customer/new-claim' },
    { label: 'My Profile', link: '/customer/profile' },
    { label: 'FAQs & Help', link: '/faq' },
  ],
  AGENT: [
    { label: 'Dashboard', link: '/agent/dashboard' },
    { label: 'Claims', link: '/agent/claims' },
    { label: 'Communications', link: '/agent/communications' },
  ],
  SURVEYOR: [{ label: 'Dashboard', link: '/surveyor/dashboard' }],
  MANAGER: [
    { label: 'Dashboard', link: '/manager/dashboard' },
    { label: 'Approval Queue', link: '/manager/approvals' },
    { label: 'Reports & Analytics', link: '/manager/reports' },
  ],
  ADMIN: [
    { label: 'Dashboard', link: '/admin/dashboard' },
    { label: 'User Management', link: '/admin/users' },
    { label: 'Role Management', link: '/admin/roles' },
    { label: 'Claim Config', link: '/admin/documents' },
    { label: 'SLA Config', link: '/admin/sla' },
    { label: 'Approval Thresholds', link: '/admin/thresholds' },
    { label: 'Notification Templates', link: '/admin/templates' },
    { label: 'Audit Logs', link: '/admin/audit' },
  ],
};

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <div class="topbar-brand">
        <span class="brand-logo">ICMS</span>
        <span class="brand-sub">Insurance Claim Management</span>
      </div>
      <div class="topbar-user">
        <span class="user-name">{{ user()?.fullName }}</span>
        <span class="role-chip role-{{ user()?.role }}">{{ user()?.role }}</span>
        <a class="btn-logout" (click)="logout()" style="cursor:pointer">Logout</a>
      </div>
    </header>

    <div class="app-shell">
      <aside class="sidebar">
        <nav class="side-nav">
          @for (item of navItems(); track item.link) {
            <a class="nav-item" [routerLink]="item.link" routerLinkActive="active">{{ item.label }}</a>
          }
        </nav>
      </aside>

      <main class="content">
        @if (flash.current(); as f) {
          <div class="alert" [class.alert-success]="f.type === 'success'" [class.alert-error]="f.type === 'error'">
            {{ f.message }}
          </div>
        }
        <router-outlet />
      </main>
    </div>
  `,
})
export class AppShell {
  private auth = inject(AuthService);
  flash = inject(FlashService);

  user = this.auth.user;
  navItems = computed<NavItem[]>(() => {
    const role = this.auth.role();
    return role ? NAV[role] : [];
  });

  logout(): void {
    this.auth.logout();
    location.assign('/login');
  }
}
