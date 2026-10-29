import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AsyncPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';

import { EmployeeService } from '../core/employee.service';
import { MetaService } from '../core/insights.service';
import { ApiProblem, CountryCode, Employee, EmployeeRequest, EmploymentType } from '../core/models';
import { EMPLOYMENT_TYPE_LABELS } from '../shared/labels';

/** Local date as yyyy-MM-dd (what <input type="date"> and the API both use). */
function today(): string {
  const now = new Date();
  return new Date(now.getTime() - now.getTimezoneOffset() * 60_000).toISOString().slice(0, 10);
}

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, RouterLink, AsyncPipe, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatAutocompleteModule, MatButtonModule, MatIconModule, MatProgressBarModule,
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss',
})
export class EmployeeFormComponent implements OnInit {
  /** Route param (":id") bound by the router; absent when adding a new employee. */
  readonly id = input<string>();

  private readonly employees = inject(EmployeeService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly meta$ = inject(MetaService).meta$;
  readonly maxHireDate = today();
  readonly employmentTypes = Object.entries(EMPLOYMENT_TYPE_LABELS) as [EmploymentType, string][];

  readonly form = inject(FormBuilder).nonNullable.group({
    fullName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    jobTitle: ['', [Validators.required, Validators.maxLength(80)]],
    department: ['', [Validators.required, Validators.maxLength(80)]],
    country: ['' as CountryCode | '', Validators.required],
    salary: [null as number | null, [Validators.required, Validators.min(0.01), Validators.max(9_999_999_999.99)]],
    employmentType: ['FULL_TIME' as EmploymentType, Validators.required],
    hireDate: ['', Validators.required],
  });

  readonly isEdit = computed(() => !!this.id());
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);

  /** Currency shown next to the salary input follows the selected country, like the backend rule. */
  private readonly selectedCountry = toSignal(this.form.controls.country.valueChanges, { initialValue: '' });
  private readonly meta = toSignal(this.meta$);
  readonly currency = computed(
    () => this.meta()?.countries.find(c => c.code === this.selectedCountry())?.currency ?? '',
  );

  ngOnInit(): void {
    const id = this.id();
    if (!id) return;
    this.loading.set(true);
    this.employees.get(Number(id)).subscribe({
      next: employee => {
        this.form.patchValue(this.toFormValue(employee));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.formError.set('This employee could not be found. They may have been deleted.');
      },
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set(null);

    const request = this.form.getRawValue() as EmployeeRequest;
    const id = this.id();
    const call: Observable<Employee> = id ? this.employees.update(Number(id), request) : this.employees.create(request);

    call.subscribe({
      next: saved => {
        this.saving.set(false);
        this.snackBar.open(`${saved.fullName} ${id ? 'updated' : 'added'}`, 'OK', { duration: 3000 });
        this.router.navigate(['/employees']);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.showServerErrors(err);
      },
    });
  }

  /** Puts the API's field messages under the matching inputs, so the HR Manager knows what to fix. */
  private showServerErrors(err: HttpErrorResponse): void {
    const problem = err.error as ApiProblem | null;
    const fieldErrors = problem?.errors ?? {};
    for (const [field, message] of Object.entries(fieldErrors)) {
      const control = this.form.get(field);
      control?.setErrors({ server: message });
      control?.markAsTouched();
    }
    if (Object.keys(fieldErrors).length === 0) {
      this.formError.set(problem?.detail ?? 'Could not save. Please try again.');
    }
  }

  private toFormValue(e: Employee) {
    return {
      fullName: e.fullName, jobTitle: e.jobTitle, department: e.department, country: e.country,
      salary: e.salary, employmentType: e.employmentType, hireDate: e.hireDate,
    };
  }
}
