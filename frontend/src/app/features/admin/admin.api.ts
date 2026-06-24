import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AdminStats,
  AdminUser,
  ApiEnvelope,
  ApprovalThreshold,
  AuditLog,
  DocumentRequirement,
  NotificationTemplate,
  Paged,
  RoleWithCount,
  SlaConfig,
} from '../../shared/models';

export interface UsersResponse {
  users: Paged<AdminUser>;
  roles: RoleWithCount[];
}

@Injectable({ providedIn: 'root' })
export class AdminApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/admin`;
  private d<T>(o: Observable<ApiEnvelope<T>>): Observable<T> {
    return o.pipe(map((r) => r.data));
  }

  dashboard(): Observable<AdminStats> {
    return this.d(this.http.get<ApiEnvelope<AdminStats>>(`${this.base}/dashboard`));
  }

  users(q: string, role: string, page: number, size = 15): Observable<UsersResponse> {
    return this.d(this.http.get<ApiEnvelope<UsersResponse>>(`${this.base}/users`, { params: { q, role, page, size } }));
  }
  createUser(body: Record<string, unknown>): Observable<unknown> {
    return this.http.post(`${this.base}/users`, body);
  }
  updateUser(id: number, status: string, roleId: number): Observable<unknown> {
    return this.http.put(`${this.base}/users/${id}`, { status, roleId });
  }
  resetPassword(id: number, newPassword: string): Observable<unknown> {
    return this.http.post(`${this.base}/users/${id}/reset-password`, { newPassword });
  }
  roles(): Observable<RoleWithCount[]> {
    return this.d(this.http.get<ApiEnvelope<RoleWithCount[]>>(`${this.base}/roles`));
  }

  sla(): Observable<SlaConfig[]> {
    return this.d(this.http.get<ApiEnvelope<SlaConfig[]>>(`${this.base}/config/sla`));
  }
  updateSla(id: number, hours: number): Observable<unknown> {
    return this.http.put(`${this.base}/config/sla/${id}`, { hours });
  }
  thresholds(): Observable<ApprovalThreshold[]> {
    return this.d(this.http.get<ApiEnvelope<ApprovalThreshold[]>>(`${this.base}/config/thresholds`));
  }
  updateThreshold(id: number, minAmount: string, maxAmount: string): Observable<unknown> {
    return this.http.put(`${this.base}/config/thresholds/${id}`, { minAmount, maxAmount });
  }
  templates(): Observable<NotificationTemplate[]> {
    return this.d(this.http.get<ApiEnvelope<NotificationTemplate[]>>(`${this.base}/config/templates`));
  }
  updateTemplate(id: number, active: boolean, body: string): Observable<unknown> {
    return this.http.put(`${this.base}/config/templates/${id}`, { active, body });
  }
  documents(): Observable<DocumentRequirement[]> {
    return this.d(this.http.get<ApiEnvelope<DocumentRequirement[]>>(`${this.base}/config/documents`));
  }
  addDocument(body: Record<string, unknown>): Observable<unknown> {
    return this.http.post(`${this.base}/config/documents`, body);
  }
  deleteDocument(id: number): Observable<unknown> {
    return this.http.delete(`${this.base}/config/documents/${id}`);
  }

  audit(action: string, result: string, page: number, size = 15): Observable<Paged<AuditLog>> {
    return this.d(this.http.get<ApiEnvelope<Paged<AuditLog>>>(`${this.base}/audit`, { params: { action, result, page, size } }));
  }
  exportAudit(action: string, result: string): Observable<Blob> {
    return this.http.get(`${this.base}/audit/export`, { params: { action, result }, responseType: 'blob' });
  }
}
