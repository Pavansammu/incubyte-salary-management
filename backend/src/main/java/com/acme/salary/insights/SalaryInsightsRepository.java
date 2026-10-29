package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Read-only aggregation queries. All grouping happens in the database, so the API returns a few
 * summary rows instead of loading 10,000 employees into memory.
 */
public interface SalaryInsightsRepository extends Repository<Employee, Long> {

    @Query("""
            select e.country as country, e.currency as currency, count(e) as headcount,
                   min(e.salary) as minSalary, max(e.salary) as maxSalary, avg(e.salary) as avgSalary
            from Employee e
            group by e.country, e.currency
            order by e.country""")
    List<SalaryAggregate> aggregateByCountry();

    @Query("""
            select e.jobTitle as groupKey, count(e) as headcount,
                   min(e.salary) as minSalary, max(e.salary) as maxSalary, avg(e.salary) as avgSalary
            from Employee e
            where e.country = :country
            group by e.jobTitle
            order by avg(e.salary) desc, e.jobTitle""")
    List<SalaryAggregate> aggregateByJobTitle(@Param("country") Country country);

    @Query("select min(e.salary) as minSalary, max(e.salary) as maxSalary from Employee e where e.country = :country")
    Optional<SalaryRange> rangeFor(@Param("country") Country country);

    /**
     * Headcount per salary bucket. Native SQL because bucketing needs integer arithmetic on the salary;
     * this is the only SQLite-specific query in the app (CAST(... AS INTEGER) floors non-negative values).
     */
    @Query(value = """
            SELECT CAST((salary - :min) * :bands / (:max - :min) AS INTEGER) AS bucket, COUNT(*) AS headcount
            FROM employees
            WHERE country = :country
            GROUP BY bucket""", nativeQuery = true)
    List<BucketCount> countByBucket(@Param("country") String country, @Param("min") BigDecimal min,
                                    @Param("max") BigDecimal max, @Param("bands") int bands);

    @Query("select count(e) from Employee e where e.country = :country")
    long countByCountry(@Param("country") Country country);

    interface SalaryAggregate {
        Country getCountry();
        String getCurrency();
        String getGroupKey();
        long getHeadcount();
        Number getMinSalary();
        Number getMaxSalary();
        Number getAvgSalary();
    }

    interface SalaryRange {
        Number getMinSalary();
        Number getMaxSalary();
    }

    interface BucketCount {
        Integer getBucket();
        Long getHeadcount();
    }
}
