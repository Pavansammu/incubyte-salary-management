package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Answers "how does the org pay people?" — read-only, summary-level data. */
@RestController
@RequestMapping("/api/insights/countries")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping
    public List<CountrySalaryStats> byCountry() {
        return service.byCountry();
    }

    @GetMapping("/{country}/job-titles")
    public List<JobTitleSalaryStats> byJobTitle(@PathVariable Country country) {
        return service.byJobTitle(country);
    }

    @GetMapping("/{country}/distribution")
    public SalaryDistribution distribution(@PathVariable Country country) {
        return service.distribution(country);
    }
}
