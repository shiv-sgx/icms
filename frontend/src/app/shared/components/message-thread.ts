import { Component, input } from '@angular/core';
import { Communication } from '../models';

/** Read-only communication thread — ports the .thread block from claim-detail.jsp. */
@Component({
  selector: 'app-message-thread',
  imports: [],
  template: `
    <div class="thread">
      @for (m of messages(); track m.id) {
        <div class="msg" [class.msg-own]="m.senderId === currentUserId()">
          <div class="msg-meta">{{ m.senderName }} &middot; {{ m.createdAt }}</div>
          <div class="msg-body">{{ m.content }}</div>
        </div>
      } @empty {
        <p class="empty">No messages yet.</p>
      }
    </div>
  `,
})
export class MessageThread {
  messages = input.required<Communication[]>();
  currentUserId = input<number | null>(null);
}
