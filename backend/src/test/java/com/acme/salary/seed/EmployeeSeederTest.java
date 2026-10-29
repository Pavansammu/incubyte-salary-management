package com.acme.salary.seed;

import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeSeederTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmployeeRepository repository;

    @Test
    void seedsAnEmptyDatabaseAndRowsReadBackCorrectlyThroughJpa() {
        EmployeeSeeder seeder = new EmployeeSeeder(jdbcTemplate, repository, 300, 1L);

        int inserted = seeder.seedIfEmpty();

        assertThat(inserted).isEqualTo(300);
        List<Employee> employees = repository.findAll();
        assertThat(employees).hasSize(300);
        // JDBC-written rows must be readable by JPA (dates, enums, money all mapped correctly)
        Employee first = employees.get(0);
        assertThat(first.getHireDate()).isNotNull();
        assertThat(first.getCreatedAt()).isNotNull();
        assertThat(first.getCurrency()).isEqualTo(first.getCountry().currency());
        assertThat(first.getSalary()).isPositive();
    }

    @Test
    void doesNothingWhenEmployeesAlreadyExistSoRestartsAreSafe() {
        EmployeeSeeder seeder = new EmployeeSeeder(jdbcTemplate, repository, 100, 1L);
        seeder.seedIfEmpty();

        int insertedOnSecondRun = seeder.seedIfEmpty();

        assertThat(insertedOnSecondRun).isZero();
        assertThat(repository.count()).isEqualTo(100);
    }
}
