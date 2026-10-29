package com.acme.salary.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

/** API view of an employee. Keeps the JPA entity out of the HTTP contract. */
public record EmployeeResponse(
        Long id,
        String fullName,
        String jobTitle,
        String department,
        Country country,
        String countryName,
        String currency,
        BigDecimal salary,
        EmploymentType employmentType,
        LocalDate hireDate) {

    static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getFullName(), e.getJobTitle(), e.getDepartment(),
                e.getCountry(), e.getCountry().displayName(), e.getCurrency(), e.getSalary(),
                e.getEmploymentType(), e.getHireDate());
    }
}
