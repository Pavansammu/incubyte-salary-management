import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { forkJoin } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';

import { InsightsService } from '../core/insights.service';
import { CountryCode, CountrySalaryStats, JobTitleSalaryStats, SalaryDistribution } from '../core/models';

/**
 * Answers "how does ACME pay people?":
 *  1. pay per country (local currency), 2. pay per job title inside one country, 3. how pay is spread.
 * Charts are plain CSS bars: enough for 5 bands, no charting dependency, readable by screen readers.
 */
@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CurrencyPipe, DecimalPipe, MatTableModule, MatSelectModule, MatFormFieldModule, MatProgressBarModule, MatIconModule],
  templateUrl: './insights.component.html',
  styleUrl: './insights.component.scss',
})
export class InsightsComponent implements OnInit {
  private readonly insights = inject(InsightsService);

  readonly countryColumns = ['countryName', 'headcount', 'minSalary', 'averageSalary', 'maxSalary'];
  readonly titleColumns = ['jobTitle', 'headcount', 'minSalary', 'averageSalary', 'maxSalary'];

  readonly countries = signal<CountrySalaryStats[]>([]);
  readonly selected = signal<CountryCode | null>(null);
  readonly jobTitles = signal<JobTitleSalaryStats[]>([]);
  readonly distribution = signal<SalaryDistribution | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly totalHeadcount = computed(() => this.countries().reduce((sum, c) => sum + c.headcount, 0));
  readonly selectedStats = computed(() => this.countries().find(c => c.country === this.selected()) ?? null);
  readonly currency = computed(() => this.selectedStats()?.currency ?? 'USD');
  private readonly largestBand = computed(() => Math.max(1, ...(this.distribution()?.bands.map(b => b.headcount) ?? [])));

  ngOnInit(): void {
    this.loading.set(true);
    this.insights.byCountry().subscribe({
      next: countries => {
        this.countries.set(countries);
        this.loading.set(false);
        // start with the biggest workforce: the country HR most often asks about
        const largest = [...countries].sort((a, b) => b.headcount - a.headcount)[0];
        if (largest) this.select(largest.country);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Could not load pay insights. Please try again.');
      },
    });
  }

  select(country: CountryCode): void {
    this.selected.set(country);
    forkJoin({
      titles: this.insights.byJobTitle(country),
      distribution: this.insights.distribution(country),
    }).subscribe({
      next: ({ titles, distribution }) => {
        this.jobTitles.set(titles);
        this.distribution.set(distribution);
      },
      error: () => this.error.set('Could not load details for this country.'),
    });
  }

  /** Bar length as a percentage of the busiest band, so the tallest bar always fills the row. */
  barWidth(headcount: number): number {
    return Math.round((headcount / this.largestBand()) * 100);
  }
}
