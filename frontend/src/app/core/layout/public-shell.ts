import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/** Centered public layout for unauthenticated pages — ports public-layout.jsp. */
@Component({
  selector: 'app-public-shell',
  imports: [RouterOutlet],
  template: `
    <div class="public-body">
      <div class="public-wrap">
        <div class="brand-mark">
          <span class="brand-logo">ICMS</span>
          <span class="brand-sub">Insurance Claim Management System</span>
        </div>
        <router-outlet />
      </div>
    </div>
  `,
})
export class PublicShell {}
