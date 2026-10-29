package com.acme.salary.employee;

import org.springframework.data.jpa.domain.Specification;

/**
 * Builds the WHERE clause for the employee list from optional filters.
 * Each filter is added only when present, so one query handles every combination.
 */
final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeSearchCriteria criteria) {
        return Specification.where(nameContains(criteria.search()))
                .and(equalTo("country", criteria.country()))
                .and(equalTo("jobTitle", blankToNull(criteria.jobTitle())))
                .and(equalTo("department", blankToNull(criteria.department())));
    }

    private static Specification<Employee> nameContains(String text) {
        String term = blankToNull(text);
        if (term == null) {
            return null;
        }
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("fullName")), pattern);
    }

    private static Specification<Employee> equalTo(String field, Object value) {
        return value == null ? null : (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
