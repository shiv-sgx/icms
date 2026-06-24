import { Routes } from '@angular/router';

/** Customer portal routes (lazy-loaded standalone components). */
export const CUSTOMER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./dashboard').then((m) => m.CustomerDashboardPage) },
  { path: 'claims', loadComponent: () => import('./claims').then((m) => m.CustomerClaimsPage) },
  { path: 'claims/:id', loadComponent: () => import('./claim-detail').then((m) => m.CustomerClaimDetailPage) },
  { path: 'new-claim', loadComponent: () => import('./new-claim').then((m) => m.CustomerNewClaimPage) },
  { path: 'profile', loadComponent: () => import('./profile').then((m) => m.CustomerProfilePage) },
];
