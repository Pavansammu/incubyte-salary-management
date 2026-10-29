package com.acme.salary.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employees_country", columnList = "country"),
        @Index(name = "idx_employees_country_job_title", columnList = "country, job_title"),
        @Index(name = "idx_employees_full_name", columnList = "full_name")
})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "job_title", nullable = false, length = 80)
    private String jobTitle;

    @Column(nullable = false, length = 80)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Country country;

    /** Stored (not computed on read) so SQL can GROUP BY it; always kept in sync with {@link #country}. */
    @Column(nullable = false, length = 3)
    private String currency;

    /** Annual gross salary in {@link #currency}. BigDecimal: money must never be a double. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Employee() {
        // for JPA
    }

    public Employee(String fullName, String jobTitle, String department, Country country,
                    BigDecimal salary, EmploymentType employmentType, LocalDate hireDate) {
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.department = department;
        setCountry(country);
        this.salary = salary;
        this.employmentType = employmentType;
        this.hireDate = hireDate;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /** The only way to change country, so currency can never drift out of sync. */
    public void setCountry(Country country) {
        this.country = country;
        this.currency = country.currency();
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Country getCountry() { return country; }
    public String getCurrency() { return currency; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
