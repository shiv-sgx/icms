import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { ApiEnvelope, FaqItem } from '../../shared/models';

/** FAQs & Help — ports faq.jsp; content served by GET /auth/faq. */
@Component({
  selector: 'app-faq',
  imports: [],
  template: `
    <div class="page-head">
      <h1 class="page-title">FAQs &amp; Help</h1>
      <p class="page-sub">Common questions about filing and tracking claims</p>
    </div>
    <div class="panel">
      <div class="panel-body faq">
        @for (item of items(); track item.q) {
          <div class="faq-item">
            <h4>{{ item.q }}</h4>
            <p>{{ item.a }}</p>
          </div>
        } @empty {
          <p class="empty">No help topics available.</p>
        }
      </div>
    </div>
  `,
})
export class Faq {
  private http = inject(HttpClient);
  items = signal<FaqItem[]>([]);

  constructor() {
    this.http
      .get<ApiEnvelope<FaqItem[]>>(`${environment.apiBaseUrl}/auth/faq`)
      .subscribe((res) => this.items.set(res.data));
  }
}
