package com.acme.salary.seed;

import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Loads the demo dataset on startup when the employees table is empty.
 * Uses JDBC batch inserts in one transaction: 10,000 rows take about a second, versus minutes
 * with row-by-row JPA saves (IDENTITY ids prevent Hibernate from batching).
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class EmployeeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSeeder.class);
    private static final int BATCH_SIZE = 1_000;
    private static final String INSERT = """
            INSERT INTO employees (full_name, job_title, department, country, currency, salary,
                                   employment_type, hire_date, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    private final JdbcTemplate jdbc;
    private final EmployeeRepository repository;
    private final int count;
    private final long randomSeed;

    public EmployeeSeeder(JdbcTemplate jdbc, EmployeeRepository repository,
                          @Value("${app.seed.count:10000}") int count,
                          @Value("${app.seed.random-seed:20261029}") long randomSeed) {
        this.jdbc = jdbc;
        this.repository = repository;
        this.count = count;
        this.randomSeed = randomSeed;
    }

    /**
     * Transactional here, not only on seedIfEmpty(): a call from inside the same object bypasses the Spring
     * proxy, so without this every row auto-commits (measured: ~36 s instead of ~1 s for 10,000 rows).
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long start = System.currentTimeMillis();
        int inserted = seedIfEmpty();
        if (inserted > 0) {
            log.info("Seeded {} employees in {} ms", inserted, System.currentTimeMillis() - start);
        }
    }

    /** @return number of rows inserted (0 when data already exists) */
    @Transactional
    public int seedIfEmpty() {
        if (repository.count() > 0) {
            return 0;
        }
        List<Employee> employees = new EmployeeGenerator(randomSeed).generate(count);
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.batchUpdate(INSERT, employees, BATCH_SIZE, (ps, e) -> {
            ps.setString(1, e.getFullName());
            ps.setString(2, e.getJobTitle());
            ps.setString(3, e.getDepartment());
            ps.setString(4, e.getCountry().name());
            ps.setString(5, e.getCurrency());
            ps.setBigDecimal(6, e.getSalary());
            ps.setString(7, e.getEmploymentType().name());
            ps.setDate(8, Date.valueOf(e.getHireDate()));
            ps.setTimestamp(9, now);
            ps.setTimestamp(10, now);
        });
        return employees.size();
    }
}
