package com.acme.salary.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use SQLite, not H2
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository repository;

    @Test
    void savesEmployeeAndDerivesCurrencyFromCountry() {
        Employee saved = repository.saveAndFlush(new Employee(
                "Asha Rao", "Software Engineer", "Engineering", Country.INDIA,
                new BigDecimal("1800000.00"), EmploymentType.FULL_TIME, LocalDate.of(2022, 3, 1)));

        Employee found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getFullName()).isEqualTo("Asha Rao");
        assertThat(found.getCountry()).isEqualTo(Country.INDIA);
        assertThat(found.getCurrency()).isEqualTo("INR");
        assertThat(found.getSalary()).isEqualByComparingTo("1800000.00");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void changingCountryAlsoChangesCurrency() {
        Employee employee = new Employee(
                "Tom Hardy", "Product Manager", "Product", Country.UNITED_KINGDOM,
                new BigDecimal("75000"), EmploymentType.FULL_TIME, LocalDate.of(2021, 6, 15));

        employee.setCountry(Country.GERMANY);

        assertThat(employee.getCurrency()).isEqualTo("EUR");
    }
}
