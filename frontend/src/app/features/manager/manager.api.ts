import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AgentBundle, ApiEnvelope, Claim, ManagerDashboard, Paged, ReportTable } from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class ManagerApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/manager`;
  private d<T>(o: Observable<ApiEnvelope<T>>): Observable<T> {
    return o.pipe(map((r) => r.data));
  }

  dashboard(): Observable<ManagerDashboard> {
    return this.d(this.http.get<ApiEnvelope<ManagerDashboard>>(`${this.base}/dashboard`));
  }
  approvals(page = 1, size = 15): Observable<Paged<Claim>> {
    return this.d(this.http.get<ApiEnvelope<Paged<Claim>>>(`${this.base}/approvals`, { params: { page, size } }));
  }
  claim(id: number): Observable<AgentBundle> {
    return this.d(this.http.get<ApiEnvelope<AgentBundle>>(`${this.base}/claims/${id}`));
  }
  decide(id: number, decision: string, remarks: string): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/decision`, { decision, remarks });
  }
  override(id: number, amount: string, justification: string): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${id}/settlement/override`, { amount, justification });
  }
  reports(): Observable<ReportTable[]> {
    return this.d(this.http.get<ApiEnvelope<ReportTable[]>>(`${this.base}/reports`));
  }
  exportReport(type: string): Observable<Blob> {
    return this.http.get(`${this.base}/reports/${type}/export`, { responseType: 'blob' });
  }
}
