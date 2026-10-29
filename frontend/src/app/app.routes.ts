import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · Salary Management',
    loadComponent: () => import('./employees/employee-list.component').then(m => m.EmployeeListComponent),
  },
  {
    path: 'employees/new',
    title: 'Add employee · Salary Management',
    loadComponent: () => import('./employees/employee-form.component').then(m => m.EmployeeFormComponent),
  },
  {
    path: 'employees/:id/edit',
    title: 'Edit employee · Salary Management',
    loadComponent: () => import('./employees/employee-form.component').then(m => m.EmployeeFormComponent),
  },
  {
    path: 'insights',
    title: 'Pay insights · Salary Management',
    loadComponent: () => import('./insights/insights.component').then(m => m.InsightsComponent),
  },
  { path: '**', redirectTo: 'employees' },
];
