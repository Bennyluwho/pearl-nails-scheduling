package com.pearlnails.backend.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.pearlnails.backend.scheduling.businesshours.BusinessHoursController;
import com.pearlnails.backend.scheduling.businesshours.BusinessHoursService;

@WebMvcTest(BusinessHoursController.class)
@Import(WebConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
class WebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BusinessHoursService businessHoursService;

    @Test
    void shouldAllowBusinessHoursUpdatePreflight() throws Exception {
        mockMvc.perform(options("/api/business-hours/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
            .andExpect(header().string(
                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("PUT")))
            .andExpect(header().string(
                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("content-type")));
    }

    @Test
    void shouldRejectPreflightFromUnconfiguredOrigin() throws Exception {
        mockMvc.perform(options("/api/business-hours/1")
                .header(HttpHeaders.ORIGIN, "http://unconfigured.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
            .andExpect(status().isForbidden())
            .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
