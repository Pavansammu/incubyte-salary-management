package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.insights.SalaryInsightsRepository.BucketCount;
import com.acme.salary.insights.SalaryInsightsRepository.SalaryAggregate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InsightsService {

    static final int DISTRIBUTION_BANDS = 5;

    private final SalaryInsightsRepository repository;

    public InsightsService(SalaryInsightsRepository repository) {
        this.repository = repository;
    }

    public List<CountrySalaryStats> byCountry() {
        return repository.aggregateByCountry().stream()
                .map(row -> new CountrySalaryStats(row.getCountry(), row.getCountry().displayName(), row.getCurrency(),
                        row.getHeadcount(), money(row.getMinSalary()), money(row.getMaxSalary()),
                        money(row.getAvgSalary())))
                .toList();
    }

    public List<JobTitleSalaryStats> byJobTitle(Country country) {
        return repository.aggregateByJobTitle(country).stream()
                .map(row -> new JobTitleSalaryStats(row.getGroupKey(), row.getHeadcount(),
                        money(row.getMinSalary()), money(row.getMaxSalary()), money(row.getAvgSalary())))
                .toList();
    }

    public SalaryDistribution distribution(Country country) {
        if (repository.countByCountry(country) == 0) {
            return new SalaryDistribution(country, country.currency(), List.of());
        }
        var range = repository.rangeFor(country).orElseThrow();
        BigDecimal min = money(range.getMinSalary());
        BigDecimal max = money(range.getMaxSalary());

        Map<Integer, Long> counts = min.compareTo(max) == 0
                ? Map.of(0, repository.countByCountry(country))
                : repository.countByBucket(country.name(), min, max, DISTRIBUTION_BANDS).stream()
                        .collect(Collectors.toMap(BucketCount::getBucket, BucketCount::getHeadcount));

        return new SalaryDistribution(country, country.currency(),
                SalaryBands.build(min, max, DISTRIBUTION_BANDS, counts));
    }

    /** SQLite returns numbers as long/double; normalise to money with 2 decimals. */
    private static BigDecimal money(Number value) {
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
    }
}
