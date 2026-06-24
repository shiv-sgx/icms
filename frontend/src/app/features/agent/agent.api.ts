import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AgentBundle,
  AgentDashboard,
  ApiEnvelope,
  Claim,
  Communication,
  Paged,
  SettlementScreen,
} from '../../shared/models';

export interface ClaimFilter {
  status?: string;
  type?: string;
  q?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class AgentApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/agent`;
  private d<T>(o: Observable<ApiEnvelope<T>>): Observable<T> {
    return o.pipe(map((r) => r.data));
  }

  dashboard(): Observable<AgentDashboard> {
    return this.d(this.http.get<ApiEnvelope<AgentDashboard>>(`${this.base}/dashboard`));
  }

  claims(f: ClaimFilter): Observable<Paged<Claim>> {
    const params: Record<string, string | number> = {};
    if (f.status) params['status'] = f.status;
    if (f.type) params['type'] = f.type;
    if (f.q) params['q'] = f.q;
    params['page'] = f.page ?? 1;
    params['size'] = f.size ?? 15;
    return this.d(this.http.get<ApiEnvelope<Paged<Claim>>>(`${this.base}/claims`, { params }));
  }

  claim(id: number): Observable<AgentBundle> {
    return this.d(this.http.get<ApiEnvelope<AgentBundle>>(`${this.base}/claims/${id}`));
  }

  acknowledge(id: number): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/acknowledge`, {});
  }
  assignSurveyor(id: number, surveyorId: number): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/assign-surveyor`, { surveyorId });
  }
  forward(id: number): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/forward`, {});
  }
  saveNote(id: number, notes: string): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/notes`, { notes });
  }
  postMessage(id: number, content: string): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/messages`, { content });
  }
  communications(): Observable<Communication[]> {
    return this.d(this.http.get<ApiEnvelope<Communication[]>>(`${this.base}/communications`));
  }

  settlement(id: number): Observable<SettlementScreen> {
    return this.d(this.http.get<ApiEnvelope<SettlementScreen>>(`${this.base}/claims/${id}/settlement`));
  }
  authorizeSettlement(id: number, body: Record<string, unknown>): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/settlement`, body);
  }
  advanceSettlement(id: number): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/settlement/advance`, {});
  }
}
