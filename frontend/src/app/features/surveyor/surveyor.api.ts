import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiEnvelope, AssessScreen, SurveyorDashboard } from '../../shared/models';

@Injectable({ providedIn: 'root' })
export class SurveyorApi {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/surveyor`;
  private d<T>(o: Observable<ApiEnvelope<T>>): Observable<T> {
    return o.pipe(map((r) => r.data));
  }

  dashboard(page = 1, size = 15): Observable<SurveyorDashboard> {
    return this.d(
      this.http.get<ApiEnvelope<SurveyorDashboard>>(`${this.base}/dashboard`, { params: { page, size } })
    );
  }

  assessment(claimId: number): Observable<AssessScreen> {
    return this.d(this.http.get<ApiEnvelope<AssessScreen>>(`${this.base}/claims/${claimId}/assessment`));
  }

  submitAssessment(claimId: number, body: Record<string, unknown>): Observable<ApiEnvelope<{ netPayable: string; message: string }>> {
    return this.http.post<ApiEnvelope<{ netPayable: string; message: string }>>(
      `${this.base}/claims/${claimId}/assessment`,
      body
    );
  }

  uploadReport(claimId: number, docType: string, file: File): Observable<unknown> {
    const fd = new FormData();
    fd.append('docType', docType);
    fd.append('file', file);
    return this.http.post(`${this.base}/claims/${claimId}/report`, fd);
  }
}
