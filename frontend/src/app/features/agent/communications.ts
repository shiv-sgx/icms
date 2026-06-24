import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AgentApi } from './agent.api';
import { Communication } from '../../shared/models';

/** Communication Center — ports agent/communications.jsp (recent messages feed). */
@Component({
  selector: 'app-agent-communications',
  imports: [RouterLink],
  template: `
    <div class="page-head">
      <h1 class="page-title">Communication Center</h1>
      <p class="page-sub">Recent messages across all claims</p>
    </div>

    <div class="panel">
      <div class="panel-body no-pad">
        <table class="table">
          <thead><tr><th>Claim</th><th>From</th><th>Channel</th><th>Message</th><th>When</th></tr></thead>
          <tbody>
            @for (m of messages(); track m.id) {
              <tr>
                <td><a [routerLink]="['/agent/claims', m.claimId]">{{ m.claimNo }}</a></td>
                <td>{{ m.senderName }}</td>
                <td><span class="pill pill-muted">{{ m.channel }}</span></td>
                <td>{{ m.content }}</td>
                <td class="muted small">{{ m.createdAt }}</td>
              </tr>
            } @empty {
              <tr><td colspan="5" class="empty">No messages.</td></tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class AgentCommunicationsPage {
  private api = inject(AgentApi);
  messages = signal<Communication[]>([]);
  constructor() {
    this.api.communications().subscribe((m) => this.messages.set(m));
  }
}
