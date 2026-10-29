package com.acme.salary.meta;

import com.acme.salary.employee.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetaController.class)
class MetaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EmployeeRepository repository;

    @Test
    void returnsDropdownOptionsIncludingCurrencyPerCountry() throws Exception {
        when(repository.findDistinctJobTitles()).thenReturn(List.of("Data Analyst", "Software Engineer"));
        when(repository.findDistinctDepartments()).thenReturn(List.of("Engineering"));

        mvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[0].code").value("INDIA"))
                .andExpect(jsonPath("$.countries[0].currency").value("INR"))
                .andExpect(jsonPath("$.employmentTypes[0]").value("FULL_TIME"))
                .andExpect(jsonPath("$.jobTitles[1]").value("Software Engineer"))
                .andExpect(jsonPath("$.departments[0]").value("Engineering"));
    }
}
