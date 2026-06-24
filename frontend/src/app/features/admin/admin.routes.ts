import { Routes } from '@angular/router';

/** Admin portal routes. */
export const ADMIN_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./dashboard').then((m) => m.AdminDashboardPage) },
  { path: 'users', loadComponent: () => import('./users').then((m) => m.AdminUsersPage) },
  { path: 'roles', loadComponent: () => import('./roles').then((m) => m.AdminRolesPage) },
  { path: 'sla', loadComponent: () => import('./sla').then((m) => m.AdminSlaPage) },
  { path: 'thresholds', loadComponent: () => import('./thresholds').then((m) => m.AdminThresholdsPage) },
  { path: 'templates', loadComponent: () => import('./templates').then((m) => m.AdminTemplatesPage) },
  { path: 'documents', loadComponent: () => import('./documents').then((m) => m.AdminDocumentsPage) },
  { path: 'audit', loadComponent: () => import('./audit').then((m) => m.AdminAuditPage) },
];
