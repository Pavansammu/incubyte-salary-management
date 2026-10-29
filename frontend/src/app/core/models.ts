// Mirrors the backend's JSON contract (see docs/DESIGN.md, section 3).

export type CountryCode =
  | 'INDIA' | 'UNITED_STATES' | 'UNITED_KINGDOM' | 'GERMANY'
  | 'CANADA' | 'AUSTRALIA' | 'SINGAPORE' | 'JAPAN';

export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT';

export interface Employee {
  id: number;
  fullName: string;
  jobTitle: string;
  department: string;
  country: CountryCode;
  currency: string;
  salary: number;
  employmentType: EmploymentType;
  hireDate: string; // yyyy-MM-dd
}

/** What the HR Manager submits; currency is derived from country on the server. */
export type EmployeeRequest = Omit<Employee, 'id' | 'currency'>;

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface EmployeeQuery {
  search?: string;
  country?: CountryCode | '';
  jobTitle?: string;
  department?: string;
  page: number;
  size: number;
  sort: string; // e.g. "fullName,asc"
}

export interface CountryOption {
  code: CountryCode;
  name: string;
  currency: string;
}

export interface Meta {
  countries: CountryOption[];
  employmentTypes: EmploymentType[];
  jobTitles: string[];
  departments: string[];
}

export interface CountrySalaryStats {
  country: CountryCode;
  countryName: string;
  currency: string;
  headcount: number;
  minSalary: number;
  maxSalary: number;
  averageSalary: number;
}

export interface JobTitleSalaryStats {
  jobTitle: string;
  headcount: number;
  minSalary: number;
  maxSalary: number;
  averageSalary: number;
}

export interface SalaryBand {
  from: number;
  to: number;
  headcount: number;
}

export interface SalaryDistribution {
  country: CountryCode;
  currency: string;
  bands: SalaryBand[];
}

/** RFC 7807 error body returned by the API. */
export interface ApiProblem {
  status: number;
  title: string;
  detail?: string;
  errors?: Record<string, string>;
}
