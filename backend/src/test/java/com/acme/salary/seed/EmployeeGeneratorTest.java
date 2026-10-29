package com.acme.salary.seed;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeGeneratorTest {

    private final EmployeeGenerator generator = new EmployeeGenerator(42L);

    @Test
    void generatesTheRequestedNumberOfEmployees() {
        assertThat(generator.generate(1_000)).hasSize(1_000);
    }

    @Test
    void sameSeedProducesTheSameDataSoDemosAreReproducible() {
        assertThat(summary(new EmployeeGenerator(7L).generate(50)))
                .isEqualTo(summary(new EmployeeGenerator(7L).generate(50)));
    }

    private static List<String> summary(List<Employee> employees) {
        return employees.stream()
                .map(e -> e.getFullName() + "|" + e.getJobTitle() + "|" + e.getCountry() + "|" + e.getSalary())
                .toList();
    }

    @Test
    void everyEmployeeIsValidAndPaidInTheirCountrysCurrency() {
        for (Employee e : generator.generate(2_000)) {
            assertThat(e.getFullName()).isNotBlank();
            assertThat(e.getJobTitle()).isNotBlank();
            assertThat(e.getDepartment()).isNotBlank();
            assertThat(e.getSalary()).isGreaterThan(BigDecimal.ZERO);
            assertThat(e.getCurrency()).isEqualTo(e.getCountry().currency());
            assertThat(e.getHireDate()).isBefore(LocalDate.of(2026, 7, 1));
        }
    }

    @Test
    void employeesAreSpreadAcrossAllCountriesWithIndiaTheLargest() {
        Map<Country, Long> byCountry = generator.generate(10_000).stream()
                .collect(Collectors.groupingBy(Employee::getCountry, Collectors.counting()));

        assertThat(byCountry).containsOnlyKeys(Country.values());
        Country largest = byCountry.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        assertThat(largest).isEqualTo(Country.INDIA);
    }

    @Test
    void seniorRolesEarnMoreThanJuniorRolesOnAverage() {
        Map<String, Double> avgByTitle = generator.generate(10_000).stream()
                .filter(e -> e.getCountry() == Country.UNITED_STATES)
                .collect(Collectors.groupingBy(Employee::getJobTitle,
                        Collectors.averagingDouble(e -> e.getSalary().doubleValue())));

        assertThat(avgByTitle.get("Engineering Manager")).isGreaterThan(avgByTitle.get("Software Engineer"));
        assertThat(avgByTitle.get("Software Engineer")).isGreaterThan(avgByTitle.get("Customer Support Specialist"));
    }
}
