package com.acme.salary.insights;

import com.acme.salary.employee.Country;

import java.util.List;

public record SalaryDistribution(Country country, String currency, List<SalaryBand> bands) {
}
