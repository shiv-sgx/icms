import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiEnvelope,
  Claim,
  ClaimBundle,
  CustomerDashboard,
  Paged,
  Policy,
  Policyholder,
} from '../../shared/models';

export interface ProfileResponse {
  account: { fullName: string; username: string; email?: string };
  hasProfile: boolean;
  policyholder: Policyholder | null;
  policies: Policy[];
}

export interface CreateClaimResult {
  id: number;
  claimNo: string;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class CustomerApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/customer`;

  private data<T>(o: Observable<ApiEnvelope<T>>): Observable<T> {
    return o.pipe(map((r) => r.data));
  }

  dashboard(): Observable<CustomerDashboard> {
    return this.data(this.http.get<ApiEnvelope<CustomerDashboard>>(`${this.base}/dashboard`));
  }

  claims(page: number, size: number): Observable<Paged<Claim>> {
    return this.data(
      this.http.get<ApiEnvelope<Paged<Claim>>>(`${this.base}/claims`, { params: { page, size } })
    );
  }

  claim(id: number): Observable<ClaimBundle> {
    return this.data(this.http.get<ApiEnvelope<ClaimBundle>>(`${this.base}/claims/${id}`));
  }

  policies(): Observable<Policy[]> {
    return this.data(this.http.get<ApiEnvelope<Policy[]>>(`${this.base}/policies`));
  }

  profile(): Observable<ProfileResponse> {
    return this.data(this.http.get<ApiEnvelope<ProfileResponse>>(`${this.base}/profile`));
  }

  createClaim(body: Record<string, unknown>): Observable<CreateClaimResult> {
    return this.data(this.http.post<ApiEnvelope<CreateClaimResult>>(`${this.base}/claims`, body));
  }

  postMessage(claimId: number, content: string): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${claimId}/messages`, { content });
  }

  withdraw(claimId: number): Observable<unknown> {
    return this.http.post(`${this.base}/claims/${claimId}/withdraw`, {});
  }
}
