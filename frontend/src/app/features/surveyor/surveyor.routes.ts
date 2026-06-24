import { Routes } from '@angular/router';

/** Surveyor portal routes. */
export const SURVEYOR_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'dashboard', loadComponent: () => import('./dashboard').then((m) => m.SurveyorDashboardPage) },
  { path: 'claims/:id/assess', loadComponent: () => import('./assess').then((m) => m.SurveyorAssessPage) },
];
