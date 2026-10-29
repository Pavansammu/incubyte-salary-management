package com.acme.salary.insights;

import java.math.BigDecimal;

/** Pay for one job title within a single country. */
public record JobTitleSalaryStats(
        String jobTitle,
        long headcount,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        BigDecimal averageSalary) {
}
