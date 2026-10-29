package com.acme.salary.seed;

import com.acme.salary.employee.Country;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmploymentType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generates realistic, reproducible employees. Pure Java (no Spring, no DB) so it is easy to test.
 * Same seed => same people, which keeps demos and bug reports reproducible.
 */
public class EmployeeGenerator {

    /** Share of headcount per country (weights, not percentages). */
    private static final Map<Country, Integer> COUNTRY_WEIGHTS = Map.of(
            Country.INDIA, 35, Country.UNITED_STATES, 25, Country.UNITED_KINGDOM, 10, Country.GERMANY, 8,
            Country.CANADA, 7, Country.AUSTRALIA, 5, Country.SINGAPORE, 5, Country.JAPAN, 5);

    /** Annual pay for a mid-level role (multiplier 1.0) in local currency. */
    private static final Map<Country, Integer> BASE_SALARY = Map.of(
            Country.INDIA, 1_500_000, Country.UNITED_STATES, 120_000, Country.UNITED_KINGDOM, 65_000,
            Country.GERMANY, 70_000, Country.CANADA, 95_000, Country.AUSTRALIA, 110_000,
            Country.SINGAPORE, 90_000, Country.JAPAN, 7_000_000);

    private record Role(String title, String department, double payMultiplier, int weight) {
    }

    private static final List<Role> ROLES = List.of(
            new Role("Software Engineer", "Engineering", 1.00, 25),
            new Role("Senior Software Engineer", "Engineering", 1.40, 12),
            new Role("Engineering Manager", "Engineering", 1.90, 3),
            new Role("QA Engineer", "Engineering", 0.80, 6),
            new Role("DevOps Engineer", "Engineering", 1.10, 4),
            new Role("Data Analyst", "Analytics", 0.80, 6),
            new Role("Data Scientist", "Analytics", 1.30, 3),
            new Role("Product Manager", "Product", 1.50, 4),
            new Role("UX Designer", "Design", 0.90, 3),
            new Role("HR Specialist", "Human Resources", 0.60, 4),
            new Role("Recruiter", "Human Resources", 0.55, 3),
            new Role("Accountant", "Finance", 0.70, 4),
            new Role("Financial Analyst", "Finance", 0.85, 3),
            new Role("Sales Executive", "Sales", 0.65, 8),
            new Role("Account Manager", "Sales", 0.90, 5),
            new Role("Customer Support Specialist", "Customer Support", 0.45, 7),
            new Role("Marketing Specialist", "Marketing", 0.70, 4),
            new Role("Legal Counsel", "Legal", 1.30, 1));

    /** Fixed window (not "today") so the generated data never changes between runs. */
    private static final LocalDate EARLIEST_HIRE = LocalDate.of(2012, 1, 1);
    private static final LocalDate LATEST_HIRE = LocalDate.of(2026, 6, 30);

    private final Random random;

    public EmployeeGenerator(long seed) {
        this.random = new Random(seed);
    }

    public List<Employee> generate(int count) {
        List<Employee> employees = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            employees.add(next());
        }
        return employees;
    }

    private Employee next() {
        Country country = pickWeighted(COUNTRY_WEIGHTS);
        Role role = pickRole();
        EmploymentType type = pickEmploymentType();
        return new Employee(NamePool.fullName(country, random), role.title(), role.department(), country,
                salary(country, role, type), type, hireDate());
    }

    private BigDecimal salary(Country country, Role role, EmploymentType type) {
        double variance = 0.80 + random.nextDouble() * 0.45;          // 80%..125% of the role's typical pay
        double partTimeFactor = type == EmploymentType.PART_TIME ? 0.5 : 1.0;
        double amount = BASE_SALARY.get(country) * role.payMultiplier() * variance * partTimeFactor;
        BigDecimal roundTo = BigDecimal.valueOf(country == Country.JAPAN ? 10_000 : 100);
        return BigDecimal.valueOf(amount).divide(roundTo, 0, RoundingMode.HALF_UP).multiply(roundTo)
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    private EmploymentType pickEmploymentType() {
        int roll = random.nextInt(100);
        if (roll < 85) return EmploymentType.FULL_TIME;
        if (roll < 92) return EmploymentType.PART_TIME;
        return EmploymentType.CONTRACT;
    }

    private LocalDate hireDate() {
        long days = LATEST_HIRE.toEpochDay() - EARLIEST_HIRE.toEpochDay();
        return EARLIEST_HIRE.plusDays((long) (random.nextDouble() * days));
    }

    private Role pickRole() {
        int total = ROLES.stream().mapToInt(Role::weight).sum();
        int roll = random.nextInt(total);
        for (Role role : ROLES) {
            roll -= role.weight();
            if (roll < 0) return role;
        }
        throw new IllegalStateException("weights exhausted");
    }

    private Country pickWeighted(Map<Country, Integer> weights) {
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(total);
        // iterate in enum order (Map.of has no stable order) to stay deterministic
        for (Country country : Country.values()) {
            roll -= weights.get(country);
            if (roll < 0) return country;
        }
        throw new IllegalStateException("weights exhausted");
    }
}
