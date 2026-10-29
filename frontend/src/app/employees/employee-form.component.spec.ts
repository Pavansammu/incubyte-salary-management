import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { EmployeeFormComponent } from './employee-form.component';
import { EmployeeService } from '../core/employee.service';
import { MetaService } from '../core/insights.service';
import { Employee } from '../core/models';

const META = {
  countries: [
    { code: 'INDIA', name: 'India', currency: 'INR' },
    { code: 'GERMANY', name: 'Germany', currency: 'EUR' },
  ],
  employmentTypes: ['FULL_TIME'],
  jobTitles: ['Software Engineer'],
  departments: ['Engineering'],
};

const ASHA: Employee = {
  id: 7, fullName: 'Asha Rao', jobTitle: 'Software Engineer', department: 'Engineering', country: 'INDIA',
  countryName: 'India', currency: 'INR', salary: 1800000, employmentType: 'FULL_TIME', hireDate: '2022-03-01',
};

describe('EmployeeFormComponent', () => {
  let fixture: ComponentFixture<EmployeeFormComponent>;
  let component: EmployeeFormComponent;
  let employees: jasmine.SpyObj<EmployeeService>;
  let router: Router;

  async function create(id?: string) {
    employees = jasmine.createSpyObj<EmployeeService>('EmployeeService', ['get', 'create', 'update']);
    employees.get.and.returnValue(of(ASHA));

    await TestBed.configureTestingModule({
      imports: [EmployeeFormComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: EmployeeService, useValue: employees },
        { provide: MetaService, useValue: { meta$: of(META) } },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(EmployeeFormComponent);
    component = fixture.componentInstance;
    if (id) fixture.componentRef.setInput('id', id);
    fixture.detectChanges();
  }

  function fillValidForm() {
    component.form.setValue({
      fullName: 'Asha Rao', jobTitle: 'Software Engineer', department: 'Engineering', country: 'INDIA',
      salary: 1800000, employmentType: 'FULL_TIME', hireDate: '2022-03-01',
    });
  }

  it('does not call the API while required fields are missing', async () => {
    await create();

    component.save();

    expect(employees.create).not.toHaveBeenCalled();
    expect(component.form.controls.fullName.touched).toBeTrue();
  });

  it('rejects a zero salary before it reaches the server', async () => {
    await create();
    fillValidForm();
    component.form.controls.salary.setValue(0);

    expect(component.form.controls.salary.hasError('min')).toBeTrue();
  });

  it('shows the currency of the selected country next to the salary', async () => {
    await create();

    component.form.controls.country.setValue('GERMANY');

    expect(component.currency()).toBe('EUR');
  });

  it('creates a new employee and returns to the list', async () => {
    await create();
    employees.create.and.returnValue(of(ASHA));
    fillValidForm();

    component.save();

    expect(employees.create).toHaveBeenCalledWith(jasmine.objectContaining({ fullName: 'Asha Rao', country: 'INDIA' }));
    expect(router.navigate).toHaveBeenCalledWith(['/employees']);
  });

  it('in edit mode, loads the employee and saves with update', async () => {
    await create('7');
    employees.update.and.returnValue(of(ASHA));

    expect(employees.get).toHaveBeenCalledWith(7);
    expect(component.form.controls.fullName.value).toBe('Asha Rao');

    component.save();

    expect(employees.update).toHaveBeenCalledWith(7, jasmine.objectContaining({ salary: 1800000 }));
  });

  it('puts server validation messages under the matching field', async () => {
    await create();
    fillValidForm();
    employees.create.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 400,
      error: { status: 400, title: 'Validation failed', errors: { hireDate: 'must be a date in the past or in the present' } },
    })));

    component.save();

    expect(component.form.controls.hireDate.getError('server')).toBe('must be a date in the past or in the present');
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
