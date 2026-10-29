package com.acme.salary.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository);
    }

    @Test
    void createSavesEmployeeAndReturnsItWithLocalCurrency() {
        when(repository.save(any(Employee.class))).thenAnswer(call -> call.getArgument(0));

        EmployeeResponse created = service.create(request("Asha Rao", Country.INDIA, "1800000"));

        assertThat(created.fullName()).isEqualTo("Asha Rao");
        assertThat(created.country()).isEqualTo(Country.INDIA);
        assertThat(created.currency()).isEqualTo("INR");
        assertThat(created.salary()).isEqualByComparingTo("1800000");
    }

    @Test
    void createTrimsNamesSoSearchAndGroupingStayClean() {
        when(repository.save(any(Employee.class))).thenAnswer(call -> call.getArgument(0));

        EmployeeResponse created = service.create(new EmployeeRequest(
                "  Asha Rao ", " Software Engineer ", " Engineering ", Country.INDIA,
                new BigDecimal("100"), EmploymentType.FULL_TIME, LocalDate.of(2022, 1, 1)));

        assertThat(created.fullName()).isEqualTo("Asha Rao");
        assertThat(created.jobTitle()).isEqualTo("Software Engineer");
        assertThat(created.department()).isEqualTo("Engineering");
    }

    @Test
    void getReturnsTheEmployee() {
        when(repository.findById(7L)).thenReturn(Optional.of(employee("Tom Hardy", Country.UNITED_KINGDOM)));

        assertThat(service.get(7L).fullName()).isEqualTo("Tom Hardy");
    }

    @Test
    void getUnknownEmployeeThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateChangesFieldsAndKeepsCurrencyInSyncWithCountry() {
        Employee existing = employee("Tom Hardy", Country.UNITED_KINGDOM);
        when(repository.findById(7L)).thenReturn(Optional.of(existing));

        EmployeeResponse updated = service.update(7L, request("Tom Hardy", Country.GERMANY, "90000"));

        assertThat(updated.country()).isEqualTo(Country.GERMANY);
        assertThat(updated.currency()).isEqualTo("EUR");
        assertThat(updated.salary()).isEqualByComparingTo("90000");
    }

    @Test
    void updateUnknownEmployeeThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("X Y", Country.INDIA, "1")))
                .isInstanceOf(EmployeeNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingEmployee() {
        when(repository.existsById(7L)).thenReturn(true);

        service.delete(7L);

        verify(repository).deleteById(7L);
    }

    @Test
    void deleteUnknownEmployeeThrowsNotFoundAndDeletesNothing() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(EmployeeNotFoundException.class);
        verify(repository, never()).deleteById(any());
    }

    private static EmployeeRequest request(String name, Country country, String salary) {
        return new EmployeeRequest(name, "Software Engineer", "Engineering", country,
                new BigDecimal(salary), EmploymentType.FULL_TIME, LocalDate.of(2022, 1, 1));
    }

    private static Employee employee(String name, Country country) {
        return new Employee(name, "Software Engineer", "Engineering", country,
                new BigDecimal("50000"), EmploymentType.FULL_TIME, LocalDate.of(2020, 5, 1));
    }
}
