package com.acme.salary.insights;

import com.acme.salary.employee.Country;

import java.math.BigDecimal;

/** How one country pays its people, in that country's currency. */
public record CountrySalaryStats(
        Country country,
        String countryName,
        String currency,
        long headcount,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        BigDecimal averageSalary) {
}
