import { Routes } from '@angular/router';

/** Agent portal routes. */
export const AGENT_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./dashboard').then((m) => m.AgentDashboardPage) },
  { path: 'claims', loadComponent: () => import('./claims').then((m) => m.AgentClaimsPage) },
  { path: 'claims/:id', loadComponent: () => import('./claim-detail').then((m) => m.AgentClaimDetailPage) },
  { path: 'claims/:id/settlement', loadComponent: () => import('./settlement').then((m) => m.AgentSettlementPage) },
  { path: 'communications', loadComponent: () => import('./communications').then((m) => m.AgentCommunicationsPage) },
];
