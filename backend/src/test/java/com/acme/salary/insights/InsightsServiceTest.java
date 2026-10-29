package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmploymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real aggregation SQL on SQLite against a tiny dataset whose answers are easy to check by hand:
 * India: Software Engineers on 1,000 and 4,000, a Data Analyst on 2,000. UK: one Software Engineer on 50,000.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InsightsServiceTest {

    @Autowired
    private EmployeeRepository employees;

    @Autowired
    private SalaryInsightsRepository insightsRepository;

    private InsightsService service;

    @BeforeEach
    void setUp() {
        service = new InsightsService(insightsRepository);
        employees.save(employee("Software Engineer", Country.INDIA, "1000"));
        employees.save(employee("Software Engineer", Country.INDIA, "4000"));
        employees.save(employee("Data Analyst", Country.INDIA, "2000"));
        employees.save(employee("Software Engineer", Country.UNITED_KINGDOM, "50000"));
    }

    @Test
    void summarisesPayPerCountryInLocalCurrency() {
        List<CountrySalaryStats> stats = service.byCountry();

        assertThat(stats).hasSize(2);
        CountrySalaryStats india = stats.get(0);
        assertThat(india.country()).isEqualTo(Country.INDIA);
        assertThat(india.countryName()).isEqualTo("India");
        assertThat(india.currency()).isEqualTo("INR");
        assertThat(india.headcount()).isEqualTo(3);
        assertThat(india.minSalary()).isEqualByComparingTo("1000");
        assertThat(india.maxSalary()).isEqualByComparingTo("4000");
        assertThat(india.averageSalary()).isEqualByComparingTo("2333.33");

        CountrySalaryStats uk = stats.get(1);
        assertThat(uk.currency()).isEqualTo("GBP");
        assertThat(uk.averageSalary()).isEqualByComparingTo("50000");
    }

    @Test
    void listsJobTitlesInACountryHighestAveragePayFirst() {
        List<JobTitleSalaryStats> titles = service.byJobTitle(Country.INDIA);

        assertThat(titles).extracting(JobTitleSalaryStats::jobTitle)
                .containsExactly("Software Engineer", "Data Analyst");
        JobTitleSalaryStats engineers = titles.get(0);
        assertThat(engineers.headcount()).isEqualTo(2);
        assertThat(engineers.minSalary()).isEqualByComparingTo("1000");
        assertThat(engineers.maxSalary()).isEqualByComparingTo("4000");
        assertThat(engineers.averageSalary()).isEqualByComparingTo("2500");
    }

    @Test
    void distributesACountrysHeadcountIntoSalaryBands() {
        SalaryDistribution distribution = service.distribution(Country.INDIA);

        assertThat(distribution.currency()).isEqualTo("INR");
        // range 1,000..4,000 in 5 bands of 600: 1,000 -> band 0, 2,000 -> band 1, 4,000 -> last band
        assertThat(distribution.bands()).extracting(SalaryBand::headcount).containsExactly(1L, 1L, 0L, 0L, 1L);
    }

    @Test
    void countryWithNoEmployeesHasNoTitlesAndNoBands() {
        assertThat(service.byJobTitle(Country.JAPAN)).isEmpty();
        assertThat(service.distribution(Country.JAPAN).bands()).isEmpty();
    }

    private static Employee employee(String title, Country country, String salary) {
        return new Employee("Test Person", title, "Engineering", country,
                new BigDecimal(salary), EmploymentType.FULL_TIME, LocalDate.of(2021, 1, 1));
    }
}
