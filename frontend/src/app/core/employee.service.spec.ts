import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EmployeeService } from './employee.service';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(EmployeeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends paging and sorting, plus only the filters that are set', () => {
    service.search({ page: 2, size: 50, sort: 'salary,desc', search: '  rao ', country: 'INDIA', jobTitle: '' })
      .subscribe();

    const req = http.expectOne(r => r.url === '/api/employees');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('50');
    expect(req.request.params.get('sort')).toBe('salary,desc');
    expect(req.request.params.get('search')).toBe('rao');
    expect(req.request.params.get('country')).toBe('INDIA');
    expect(req.request.params.has('jobTitle')).toBeFalse();
    expect(req.request.params.has('department')).toBeFalse();
    req.flush({ content: [], page: 2, size: 50, totalElements: 0, totalPages: 0 });
  });

  it('uses the right HTTP verbs for create, update and delete', () => {
    const body = {
      fullName: 'Asha Rao', jobTitle: 'Software Engineer', department: 'Engineering', country: 'INDIA' as const,
      salary: 100, employmentType: 'FULL_TIME' as const, hireDate: '2022-01-01',
    };

    service.create(body).subscribe();
    expect(http.expectOne({ method: 'POST', url: '/api/employees' }).request.body).toEqual(body);

    service.update(7, body).subscribe();
    http.expectOne({ method: 'PUT', url: '/api/employees/7' }).flush({});

    service.delete(7).subscribe();
    http.expectOne({ method: 'DELETE', url: '/api/employees/7' }).flush(null);
  });
});
