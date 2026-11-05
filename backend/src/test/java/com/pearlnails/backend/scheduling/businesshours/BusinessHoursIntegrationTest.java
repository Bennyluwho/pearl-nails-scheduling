
package com.pearlnails.backend.scheduling.businesshours;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class BusinessHoursIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final AtomicInteger COUNTER =
        new AtomicInteger();

    // TEST 1: Retrieve all seven business days

    @Test
    void shouldGetAllBusinessHours() throws Exception {
        mockMvc.perform(get("/api/business-hours"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(7))
            .andExpect(jsonPath("$[0].dayOfWeek").value(1))
            .andExpect(jsonPath("$[0].closed").value(false))
            .andExpect(jsonPath("$[6].dayOfWeek").value(7))
            .andExpect(jsonPath("$[6].closed").value(true));
    }

    // TEST 2: Update business hours successfully

    @Test
    void shouldUpdateBusinessHours() throws Exception {

        putHours(1, openDay("10:00", "18:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.closed").value(false))
            .andExpect(
                jsonPath("$.openTime").value(startsWith("10:00"))
            )
            .andExpect(
                jsonPath("$.closeTime").value(startsWith("18:00"))
            );

        // Verify the update is visible through the API.

        mockMvc.perform(get("/api/business-hours"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$[0].closeTime").value(startsWith("18:00"))
            );
    }

    // TEST 3: Close a day without employee shifts

    @Test
    void shouldCloseBusinessDay() throws Exception {

        putHours(1, closedDay())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.closed").value(true))
            .andExpect(jsonPath("$.openTime").isEmpty())
            .andExpect(jsonPath("$.closeTime").isEmpty());
    }

    // TEST 4: Reopen a previously closed day

    @Test
    void shouldReopenClosedDay() throws Exception {

        // Sunday is closed by the V8 seed migration.

        putHours(7, openDay("10:00", "16:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dayOfWeek").value(7))
            .andExpect(jsonPath("$.closed").value(false))
            .andExpect(
                jsonPath("$.openTime").value(startsWith("10:00"))
            );
    }

    // TEST 5: Reject invalid opening and closing times

    @Test
    void shouldRejectInvalidTimeRange() throws Exception {

        putHours(1, openDay("19:00", "09:00"))
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Opening time must be before closing time")
            );
    }

    // TEST 6: Reject missing opening or closing times

    @Test
    void shouldRejectMissingTime() throws Exception {

        putHours(1, """
            {
              "closed": false,
              "openTime": "09:00",
              "closeTime": null
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Opening and closing times are required")
            );
    }

    // TEST 7: Reject missing closed field

    @Test
    void shouldRejectMissingClosedStatus() throws Exception {

        putHours(1, """
            {
              "openTime": "09:00",
              "closeTime": "18:00"
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message").value("Validation failed")
            )
            .andExpect(
                jsonPath("$.validationErrors.closed").exists()
            );
    }

    // TEST 8: Reject a closed day containing hours

    @Test
    void shouldRejectClosedDayWithTimes() throws Exception {

        putHours(1, """
            {
              "closed": true,
              "openTime": "09:00",
              "closeTime": "18:00"
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Closed days cannot have opening or closing times"
                    )
            );
    }

    // TEST 9: Reject invalid weekday numbers

    @Test
    void shouldRejectInvalidDayOfWeek() throws Exception {

        putHours(8, openDay("09:00", "18:00"))
            .andExpect(status().isBadRequest());

        putHours(0, openDay("09:00", "18:00"))
            .andExpect(status().isBadRequest());
    }

    // TEST 10: Reject closing a day with employee shifts

    @Test
    void shouldRejectClosingDayWithEmployeeShifts()
        throws Exception {

        Long employeeId = createEmployee();

        createShift(employeeId, 1, "10:00", "17:00");

        putHours(1, closedDay())
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Cannot close salon while employees have scheduled shifts"
                    )
            );
    }

    // TEST 11: Reject new hours that conflict with a shift

    @Test
    void shouldRejectConflictingBusinessHours()
        throws Exception {

        Long employeeId = createEmployee();

        createShift(employeeId, 1, "10:00", "18:00");

        putHours(1, openDay("09:00", "17:00"))
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "New business hours conflict with employee shifts"
                    )
            );

        // Rejected updates must not change existing hours.

        mockMvc.perform(get("/api/business-hours"))
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$[0].closeTime")
                    .value(startsWith("19:00"))
            );
    }

    // TEST 12: Allow business hours containing employee shifts

    @Test
    void shouldAllowHoursContainingEmployeeShifts()
        throws Exception {

        Long employeeId = createEmployee();

        createShift(employeeId, 1, "10:00", "17:00");

        putHours(1, openDay("09:00", "18:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.closed").value(false))
            .andExpect(
                jsonPath("$.closeTime")
                    .value(startsWith("18:00"))
            );
    }

    // TEST 13: Updating Monday must not modify Sunday

    @Test
    void shouldNotModifyOtherBusinessDays() throws Exception {

        putHours(1, openDay("10:00", "18:00"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/business-hours"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[6].dayOfWeek").value(7))
            .andExpect(jsonPath("$[6].closed").value(true));
    }

    // HELPERS

    private ResultActions putHours(
        int dayOfWeek,
        String json
    ) throws Exception {

        return mockMvc.perform(
            put("/api/business-hours/{dayOfWeek}", dayOfWeek)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );
    }

    private String openDay(String open, String close) {
        return """
            {
              "closed": false,
              "openTime": "%s",
              "closeTime": "%s"
            }
            """.formatted(open, close);
    }

    private String closedDay() {
        return """
            {
              "closed": true,
              "openTime": null,
              "closeTime": null
            }
            """;
    }

    private Long createEmployee() throws Exception {
        int number = COUNTER.incrementAndGet();

        MvcResult result = mockMvc.perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName": "BusinessHours",
                      "lastName": "Test%d"
                    }
                    """.formatted(number))
        )
        .andExpect(status().isCreated())
        .andReturn();

        return extractId(result);
    }

    private Long createShift(
        Long employeeId,
        int dayOfWeek,
        String startTime,
        String endTime
    ) throws Exception {

        MvcResult result = mockMvc.perform(
            post(
                "/api/employees/{employeeId}/working-hours",
                employeeId
            )
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "dayOfWeek": %d,
                  "startTime": "%s",
                  "endTime": "%s"
                }
                """.formatted(dayOfWeek, startTime, endTime))
        )
        .andExpect(status().isCreated())
        .andReturn();

        return extractId(result);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return json.get("id").asLong();
    }
}
