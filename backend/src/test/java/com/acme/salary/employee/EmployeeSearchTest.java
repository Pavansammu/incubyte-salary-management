package com.acme.salary.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeSearchTest {

    @Autowired
    private EmployeeRepository repository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository);
        repository.save(employee("Asha Rao", "Software Engineer", "Engineering", Country.INDIA));
        repository.save(employee("Ravi Kumar", "Data Analyst", "Analytics", Country.INDIA));
        repository.save(employee("Ashley Cole", "Software Engineer", "Engineering", Country.UNITED_KINGDOM));
        repository.save(employee("John Smith", "Product Manager", "Product", Country.UNITED_STATES));
        repository.save(employee("Priya Shah", "Software Engineer", "Engineering", Country.INDIA));
    }

    @Test
    void noCriteriaReturnsEveryoneInPages() {
        Page<EmployeeResponse> page = service.search(EmployeeSearchCriteria.none(), PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalPages()).isEqualTo(3);
    }

    @Test
    void searchMatchesPartOfTheNameIgnoringCase() {
        Page<EmployeeResponse> page = search(new EmployeeSearchCriteria("ASH", null, null, null));

        assertThat(page.getContent()).extracting(EmployeeResponse::fullName)
                .containsExactly("Asha Rao", "Ashley Cole");
    }

    @Test
    void filtersByCountry() {
        Page<EmployeeResponse> page = search(new EmployeeSearchCriteria(null, Country.INDIA, null, null));

        assertThat(page.getContent()).extracting(EmployeeResponse::fullName)
                .containsExactly("Asha Rao", "Priya Shah", "Ravi Kumar");
    }

    @Test
    void combinesFiltersWithAnd() {
        Page<EmployeeResponse> page = search(
                new EmployeeSearchCriteria(null, Country.INDIA, "Software Engineer", "Engineering"));

        assertThat(page.getContent()).extracting(EmployeeResponse::fullName)
                .containsExactly("Asha Rao", "Priya Shah");
    }

    @Test
    void blankSearchTextIsIgnored() {
        assertThat(search(new EmployeeSearchCriteria("   ", null, null, null)).getTotalElements()).isEqualTo(5);
    }

    private Page<EmployeeResponse> search(EmployeeSearchCriteria criteria) {
        return service.search(criteria, PageRequest.of(0, 20, Sort.by("fullName")));
    }

    private static Employee employee(String name, String title, String department, Country country) {
        return new Employee(name, title, department, country,
                new BigDecimal("50000"), EmploymentType.FULL_TIME, LocalDate.of(2021, 1, 1));
    }
}
