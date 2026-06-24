import { Routes } from '@angular/router';

/** Manager portal routes. */
export const MANAGER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./dashboard').then((m) => m.ManagerDashboardPage) },
  { path: 'approvals', loadComponent: () => import('./approvals').then((m) => m.ManagerApprovalsPage) },
  { path: 'claims/:id', loadComponent: () => import('./claim-detail').then((m) => m.ManagerClaimDetailPage) },
  { path: 'reports', loadComponent: () => import('./reports').then((m) => m.ManagerReportsPage) },
];
