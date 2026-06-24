import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing').then((m) => m.Landing),
  },
  // Phase 1+ routes (login, role shells, feature areas) are added here.
  { path: '**', redirectTo: '' },
];
