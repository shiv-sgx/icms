import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Access-denied page — ports error/denied.jsp. Rendered inside the app shell. */
@Component({
  selector: 'app-denied',
  imports: [RouterLink],
  template: `
    <div class="page-head">
      <h1 class="page-title">Access denied</h1>
      <p class="page-sub">You don't have permission to view that page.</p>
    </div>
    <div class="panel">
      <div class="panel-body">
        <p class="muted">If you believe this is a mistake, contact your administrator.</p>
        <a class="btn btn-primary" routerLink="/">Back to my dashboard</a>
      </div>
    </div>
  `,
})
export class Denied {}
