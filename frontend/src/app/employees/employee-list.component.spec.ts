import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { EmployeeListComponent } from './employee-list.component';
import { EmployeeService } from '../core/employee.service';
import { MetaService } from '../core/insights.service';
import { Employee, Page } from '../core/models';

const ASHA: Employee = {
  id: 1, fullName: 'Asha Rao', jobTitle: 'Software Engineer', department: 'Engineering', country: 'INDIA',
  currency: 'INR', salary: 1800000, employmentType: 'FULL_TIME', hireDate: '2022-03-01',
};

const onePage = (content: Employee[]): Page<Employee> =>
  ({ content, page: 0, size: 20, totalElements: content.length, totalPages: 1 });

describe('EmployeeListComponent', () => {
  let fixture: ComponentFixture<EmployeeListComponent>;
  let employeeService: jasmine.SpyObj<EmployeeService>;

  beforeEach(async () => {
    employeeService = jasmine.createSpyObj<EmployeeService>('EmployeeService', ['search', 'delete']);
    employeeService.search.and.returnValue(of(onePage([ASHA])));

    await TestBed.configureTestingModule({
      imports: [EmployeeListComponent],
      providers: [
        provideRouter([]),
        provideNoopAnimations(),
        { provide: EmployeeService, useValue: employeeService },
        {
          provide: MetaService,
          useValue: { meta$: of({ countries: [], employmentTypes: [], jobTitles: [], departments: [] }) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeListComponent);
    fixture.detectChanges();
  });

  it('loads the first page sorted by name and shows each employee with local-currency salary', () => {
    expect(employeeService.search).toHaveBeenCalledOnceWith({ page: 0, size: 20, sort: 'fullName,asc' });

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Asha Rao');
    expect(text).toContain('₹18,00,000'.slice(0, 1)); // rupee symbol rendered from the employee's currency
    expect(text).toContain('Full-time');
    expect(text).toContain('1 employees match your filters');
  });

  it('waits until the HR Manager stops typing, then searches from the first page', fakeAsync(() => {
    employeeService.search.calls.reset();

    fixture.componentInstance.filters.controls.search.setValue('ras');
    tick(100);
    fixture.componentInstance.filters.controls.search.setValue('rao');
    tick(300);

    expect(employeeService.search).toHaveBeenCalledTimes(1);
    expect(employeeService.search.calls.mostRecent().args[0]).toEqual(
      jasmine.objectContaining({ search: 'rao', page: 0 }));
  }));

  it('requests the selected page when the paginator changes', () => {
    employeeService.search.calls.reset();

    fixture.componentInstance.onPage({ pageIndex: 3, pageSize: 50, length: 1000 });

    expect(employeeService.search).toHaveBeenCalledOnceWith(jasmine.objectContaining({ page: 3, size: 50 }));
  });
});
