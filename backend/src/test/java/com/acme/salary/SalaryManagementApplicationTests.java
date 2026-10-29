package com.acme.salary;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:sqlite:file:ctx?mode=memory&cache=shared")
class SalaryManagementApplicationTests {

    @Test
    void contextLoads() {
    }
}
