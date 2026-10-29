import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Employee, EmployeeQuery, EmployeeRequest, Page } from './models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/employees';

  search(query: EmployeeQuery): Observable<Page<Employee>> {
    let params = new HttpParams()
      .set('page', query.page)
      .set('size', query.size)
      .set('sort', query.sort);
    // send only the filters the HR Manager actually set
    for (const key of ['search', 'country', 'jobTitle', 'department'] as const) {
      const value = query[key]?.trim();
      if (value) {
        params = params.set(key, value);
      }
    }
    return this.http.get<Page<Employee>>(this.baseUrl, { params });
  }

  get(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.baseUrl}/${id}`);
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
