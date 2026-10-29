import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { CountryCode, CountrySalaryStats, JobTitleSalaryStats, Meta, SalaryDistribution } from './models';

@Injectable({ providedIn: 'root' })
export class InsightsService {
  private readonly http = inject(HttpClient);

  byCountry(): Observable<CountrySalaryStats[]> {
    return this.http.get<CountrySalaryStats[]>('/api/insights/countries');
  }

  byJobTitle(country: CountryCode): Observable<JobTitleSalaryStats[]> {
    return this.http.get<JobTitleSalaryStats[]>(`/api/insights/countries/${country}/job-titles`);
  }

  distribution(country: CountryCode): Observable<SalaryDistribution> {
    return this.http.get<SalaryDistribution>(`/api/insights/countries/${country}/distribution`);
  }
}

@Injectable({ providedIn: 'root' })
export class MetaService {
  private readonly http = inject(HttpClient);

  /** Dropdown options rarely change during a session, so fetch once and share. */
  readonly meta$: Observable<Meta> = this.http.get<Meta>('/api/meta').pipe(shareReplay(1));
}
