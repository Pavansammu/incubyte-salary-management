package com.acme.salary.employee;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What the HR Manager submits when adding or editing an employee.
 * Currency is intentionally absent: it is derived from {@link #country()}.
 */
public record EmployeeRequest(
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @NotBlank @Size(max = 80) String jobTitle,
        @NotBlank @Size(max = 80) String department,
        @NotNull Country country,
        @NotNull @DecimalMin(value = "0.01", message = "must be greater than 0")
        @Digits(integer = 10, fraction = 2) BigDecimal salary,
        @NotNull EmploymentType employmentType,
        @NotNull @PastOrPresent LocalDate hireDate) {
}
