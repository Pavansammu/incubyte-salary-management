package com.acme.salary.insights;

import com.acme.salary.employee.Country;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsightsController.class)
class InsightsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private InsightsService service;

    @Test
    void countrySummary() throws Exception {
        when(service.byCountry()).thenReturn(List.of(new CountrySalaryStats(
                Country.INDIA, "India", "INR", 3, bd("1000"), bd("4000"), bd("2333.33"))));

        mvc.perform(get("/api/insights/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("INDIA"))
                .andExpect(jsonPath("$[0].countryName").value("India"))
                .andExpect(jsonPath("$[0].currency").value("INR"))
                .andExpect(jsonPath("$[0].averageSalary").value(2333.33));
    }

    @Test
    void jobTitlesForACountry() throws Exception {
        when(service.byJobTitle(Country.INDIA)).thenReturn(List.of(
                new JobTitleSalaryStats("Software Engineer", 2, bd("1000"), bd("4000"), bd("2500"))));

        mvc.perform(get("/api/insights/countries/INDIA/job-titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobTitle").value("Software Engineer"))
                .andExpect(jsonPath("$[0].headcount").value(2));
    }

    @Test
    void distributionForACountry() throws Exception {
        when(service.distribution(Country.INDIA)).thenReturn(new SalaryDistribution(Country.INDIA, "INR",
                List.of(new SalaryBand(bd("1000"), bd("1600"), 1))));

        mvc.perform(get("/api/insights/countries/INDIA/distribution"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.bands[0].headcount").value(1));
    }

    @Test
    void unknownCountryIsABadRequest() throws Exception {
        mvc.perform(get("/api/insights/countries/ATLANTIS/job-titles"))
                .andExpect(status().isBadRequest());
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
