package com.acme.salary.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CountryTest {

    @ParameterizedTest(name = "{0} pays in {1}")
    @CsvSource({
            "INDIA, INR",
            "UNITED_STATES, USD",
            "UNITED_KINGDOM, GBP",
            "GERMANY, EUR"
    })
    void eachCountryHasItsLocalCurrency(Country country, String expectedCurrency) {
        assertThat(country.currency()).isEqualTo(expectedCurrency);
    }

    @Test
    void everyCountryHasADisplayNameAndThreeLetterCurrency() {
        for (Country country : Country.values()) {
            assertThat(country.displayName()).isNotBlank();
            assertThat(country.currency()).matches("[A-Z]{3}");
        }
    }
}
