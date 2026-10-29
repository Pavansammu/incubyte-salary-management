package com.acme.salary.insights;

import java.math.BigDecimal;

/** Number of employees whose salary is in [from, to). The last band also includes {@code to}. */
public record SalaryBand(BigDecimal from, BigDecimal to, long headcount) {
}
