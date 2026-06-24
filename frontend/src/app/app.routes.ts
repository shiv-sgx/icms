import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { homeRedirectGuard } from './core/guards/home-redirect.guard';
import { AppShell } from './core/layout/app-shell';
import { PublicShell } from './core/layout/public-shell';
import { PagePlaceholder } from './shared/components/page-placeholder';

/** Helper: a placeholder route (real component arrives in a later phase). */
const ph = (path: string, title: string, subtitle = '') => ({
  path,
  component: PagePlaceholder,
  data: { title, subtitle },
});

export const routes: Routes = [
  // Public area (centered shell)
  {
    path: 'login',
    component: PublicShell,
    children: [{ path: '', loadComponent: () => import('./features/auth/login').then((m) => m.Login) }],
  },

  // Authenticated area (topbar + role sidebar)
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', canActivate: [homeRedirectGuard], children: [] },
      { path: 'faq', loadComponent: () => import('./features/auth/faq').then((m) => m.Faq) },
      { path: 'denied', loadComponent: () => import('./features/auth/denied').then((m) => m.Denied) },

      // Customer (Phase 2 replaces placeholders with real components)
      {
        path: 'customer',
        canActivate: [roleGuard],
        data: { role: 'CUSTOMER' },
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
          ph('dashboard', 'Dashboard'),
          ph('claims', 'My Claims'),
          ph('new-claim', 'New Claim'),
          ph('profile', 'My Profile'),
        ],
      },

      // Agent (Phase 3)
      {
        path: 'agent',
        canActivate: [roleGuard],
        data: { role: 'AGENT' },
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
          ph('dashboard', 'Dashboard'),
          ph('claims', 'Claims'),
          ph('communications', 'Communications'),
        ],
      },

      // Surveyor (Phase 3)
      {
        path: 'surveyor',
        canActivate: [roleGuard],
        data: { role: 'SURVEYOR' },
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
          ph('dashboard', 'Dashboard'),
        ],
      },

      // Manager (Phase 4)
      {
        path: 'manager',
        canActivate: [roleGuard],
        data: { role: 'MANAGER' },
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
          ph('dashboard', 'Dashboard'),
          ph('approvals', 'Approval Queue'),
          ph('reports', 'Reports & Analytics'),
        ],
      },

      // Admin (Phase 4)
      {
        path: 'admin',
        canActivate: [roleGuard],
        data: { role: 'ADMIN' },
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
          ph('dashboard', 'Dashboard'),
          ph('users', 'User Management'),
          ph('roles', 'Role Management'),
          ph('documents', 'Claim Config'),
          ph('sla', 'SLA Config'),
          ph('thresholds', 'Approval Thresholds'),
          ph('templates', 'Notification Templates'),
          ph('audit', 'Audit Logs'),
        ],
      },
    ],
  },

  { path: '**', redirectTo: '' },
];
