package com.acme.salary.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Uses a stub index.html from src/test/resources/static to stand in for the Angular build. */
@SpringBootTest
@AutoConfigureMockMvc
class SpaWebConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void clientSideRouteServesTheAppShellSoRefreshAndDeepLinksWork() throws Exception {
        mvc.perform(get("/employees/42/edit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SPA shell")));
    }

    @Test
    void rootForwardsToTheAppShell() throws Exception {
        // Spring Boot's welcome-page handling forwards "/" to index.html (MockMvc records, not follows, forwards)
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
    }

    @Test
    void unknownApiRouteIsStillA404NotTheAppShell() throws Exception {
        mvc.perform(get("/api/does-not-exist")).andExpect(status().isNotFound());
    }

    @Test
    void healthEndpointIsAvailableForTheHostingPlatform() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }
}
