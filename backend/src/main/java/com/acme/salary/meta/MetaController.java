package com.acme.salary.meta;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.EmployeeRepository;
import com.acme.salary.employee.EmploymentType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Reference data for UI dropdowns, so the UI never hard-codes countries or titles. */
@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final EmployeeRepository repository;

    public MetaController(EmployeeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Meta meta() {
        List<CountryOption> countries = Arrays.stream(Country.values())
                .map(c -> new CountryOption(c, c.displayName(), c.currency()))
                .toList();
        return new Meta(countries, List.of(EmploymentType.values()),
                repository.findDistinctJobTitles(), repository.findDistinctDepartments());
    }

    public record CountryOption(Country code, String name, String currency) {
    }

    public record Meta(List<CountryOption> countries, List<EmploymentType> employmentTypes,
                       List<String> jobTitles, List<String> departments) {
    }
}
