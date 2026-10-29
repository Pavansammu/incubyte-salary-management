package com.acme.salary.employee;

/** Optional filters for the employee list; null or blank values mean "don't filter on this". */
public record EmployeeSearchCriteria(String search, Country country, String jobTitle, String department) {

    public static EmployeeSearchCriteria none() {
        return new EmployeeSearchCriteria(null, null, null, null);
    }
}
