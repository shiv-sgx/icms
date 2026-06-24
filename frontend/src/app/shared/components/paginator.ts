import { Component, computed, input, output } from '@angular/core';

/** Prev/next pager — ports the .pager block; emits the requested page. */
@Component({
  selector: 'app-paginator',
  imports: [],
  template: `
    @if (totalPages() > 1) {
      <div class="pager">
        @if (page() > 1) {
          <button class="btn btn-light" (click)="go(page() - 1)">&laquo; Prev</button>
        }
        <span class="pager-info">Page {{ page() }} of {{ totalPages() }}</span>
        @if (page() < totalPages()) {
          <button class="btn btn-light" (click)="go(page() + 1)">Next &raquo;</button>
        }
      </div>
    }
  `,
})
export class Paginator {
  page = input.required<number>();
  size = input.required<number>();
  total = input.required<number>();
  pageChange = output<number>();

  totalPages = computed(() => Math.max(1, Math.ceil(this.total() / Math.max(1, this.size()))));

  go(p: number): void {
    this.pageChange.emit(p);
  }
}
