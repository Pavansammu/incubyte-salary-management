import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AsyncPipe, CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  BehaviorSubject, catchError, combineLatest, debounceTime, distinctUntilChanged, finalize, map, of, switchMap, tap,
} from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { EmployeeService } from '../core/employee.service';
import { MetaService } from '../core/insights.service';
import { Employee, EmployeeQuery, Page } from '../core/models';
import { ConfirmDialogComponent, ConfirmDialogData } from '../shared/confirm-dialog.component';
import { EMPLOYMENT_TYPE_LABELS } from '../shared/labels';

const DEFAULT_QUERY: EmployeeQuery = { page: 0, size: 20, sort: 'fullName,asc' };

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink, AsyncPipe, CurrencyPipe, DatePipe, DecimalPipe,
    MatTableModule, MatPaginatorModule, MatSortModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatTooltipModule, MatProgressBarModule,
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss',
})
export class EmployeeListComponent implements OnInit {
  private readonly employees = inject(EmployeeService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly destroyRef = inject(DestroyRef);

  readonly meta$ = inject(MetaService).meta$;
  readonly columns = ['fullName', 'jobTitle', 'department', 'country', 'salary', 'employmentType', 'hireDate', 'actions'];

  readonly filters = inject(FormBuilder).nonNullable.group({
    search: '',
    country: '',
    jobTitle: '',
    department: '',
  });

  readonly page = signal<Page<Employee> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  /** Single source of truth for what is on screen; every change triggers one API call. */
  private readonly query$ = new BehaviorSubject<EmployeeQuery>(DEFAULT_QUERY);
  /** Re-runs the current query, e.g. after a delete. */
  private readonly refresh$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.filters.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe(f => this.update({ ...f, country: f.country as EmployeeQuery['country'], page: 0 }));

    combineLatest([
      this.query$.pipe(distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b))),
      this.refresh$,
    ])
      .pipe(
        map(([query]) => query),
        tap(() => { this.loading.set(true); this.error.set(null); }),
        // switchMap cancels a slow older request when the HR Manager keeps typing
        switchMap(q => this.employees.search(q).pipe(
          catchError(() => {
            this.error.set('Could not load employees. Please try again.');
            return of(null);
          }),
          finalize(() => this.loading.set(false)),
        )),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(page => this.page.set(page));
  }

  typeLabel(employee: Employee): string {
    return EMPLOYMENT_TYPE_LABELS[employee.employmentType];
  }

  onPage(event: PageEvent): void {
    this.update({ page: event.pageIndex, size: event.pageSize });
  }

  onSort(sort: Sort): void {
    this.update({ sort: sort.direction ? `${sort.active},${sort.direction}` : DEFAULT_QUERY.sort, page: 0 });
  }

  clearFilters(): void {
    this.filters.reset();
  }

  confirmDelete(employee: Employee): void {
    const data: ConfirmDialogData = {
      title: 'Delete employee?',
      message: `${employee.fullName} (${employee.jobTitle}, ${employee.countryName}) will be permanently removed.`,
      confirmLabel: 'Delete',
    };
    this.dialog.open(ConfirmDialogComponent, { data }).afterClosed().subscribe(confirmed => {
      if (!confirmed) return;
      this.employees.delete(employee.id).subscribe({
        next: () => {
          this.snackBar.open(`${employee.fullName} deleted`, 'OK', { duration: 3000 });
          this.reload();
        },
        error: () => this.snackBar.open('Delete failed. Please try again.', 'OK', { duration: 4000 }),
      });
    });
  }

  private update(change: Partial<EmployeeQuery>): void {
    this.query$.next({ ...this.query$.value, ...change });
  }

  private reload(): void {
    this.refresh$.next();
  }
}
