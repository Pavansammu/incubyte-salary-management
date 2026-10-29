package com.acme.salary.employee;

/**
 * Countries ACME employs people in. Each country has exactly one payroll currency, so the
 * currency is derived from the country rather than entered by hand. This keeps per-country
 * salary insights trustworthy (no "India, USD" rows skewing an average).
 */
public enum Country {
    INDIA("India", "INR"),
    UNITED_STATES("United States", "USD"),
    UNITED_KINGDOM("United Kingdom", "GBP"),
    GERMANY("Germany", "EUR"),
    CANADA("Canada", "CAD"),
    AUSTRALIA("Australia", "AUD"),
    SINGAPORE("Singapore", "SGD"),
    JAPAN("Japan", "JPY");

    private final String displayName;
    private final String currency;

    Country(String displayName, String currency) {
        this.displayName = displayName;
        this.currency = currency;
    }

    public String displayName() {
        return displayName;
    }

    /** ISO 4217 code of the local payroll currency. */
    public String currency() {
        return currency;
    }
}
