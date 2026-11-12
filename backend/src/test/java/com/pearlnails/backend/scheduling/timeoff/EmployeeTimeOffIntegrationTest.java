
package com.pearlnails.backend.scheduling.timeoff;

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

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EmployeeTimeOffIntegrationTest {

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

    private static final String DATE = "2030-06-10";

    // TEST 1: Retrieve an employee with no absences

    @Test
    void shouldGetEmptyTimeOff() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(get(url(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // TEST 2: Create a full-day absence

    @Test
    void shouldCreateFullDayTimeOff() throws Exception {
        Long employeeId = createEmployee();

        createTimeOffRequest(
            employeeId,
            fullDayJson(DATE)
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.employeeId").value(employeeId))
            .andExpect(jsonPath("$.timeOffDate").value(DATE))
            .andExpect(jsonPath("$.fullDay").value(true))
            .andExpect(jsonPath("$.startTime").isEmpty())
            .andExpect(jsonPath("$.endTime").isEmpty());

        mockMvc.perform(get(url(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    // TEST 3: Create a partial-day absence

    @Test
    void shouldCreatePartialDayTimeOff() throws Exception {
        Long employeeId = createEmployee();

        createTimeOffRequest(
            employeeId,
            partialDayJson(DATE, "12:00", "14:00")
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fullDay").value(false))
            .andExpect(jsonPath("$.startTime").isNotEmpty())
            .andExpect(jsonPath("$.endTime").isNotEmpty());
    }

    // TEST 4: Reject missing required fields

    @Test
    void shouldRejectMissingRequiredFields() throws Exception {
        Long employeeId = createEmployee();

        createTimeOffRequest(employeeId, """
            {
              "reason": "Vacation"
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.validationErrors.timeOffDate").exists())
            .andExpect(jsonPath("$.validationErrors.fullDay").exists());
    }

    // TEST 5: Reject invalid partial-day times

    @Test
    void shouldRejectInvalidTimeRange() throws Exception {
        Long employeeId = createEmployee();

        createTimeOffRequest(
            employeeId,
            partialDayJson(DATE, "17:00", "09:00")
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Time-off start must be before end time")
            );
    }

    // TEST 6: Reject full-day absence with specific times

    @Test
    void shouldRejectFullDayWithTimes() throws Exception {
        Long employeeId = createEmployee();

        createTimeOffRequest(employeeId, """
            {
              "timeOffDate": "2030-06-10",
              "fullDay": true,
              "startTime": "09:00",
              "endTime": "17:00"
            }
            """)
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Full-day time off cannot have start or end times")
            );
    }

    // TEST 7: Reject overlapping partial-day absences

    @Test
    void shouldRejectOverlappingTimeOff() throws Exception {
        Long employeeId = createEmployee();

        createTimeOff(
            employeeId,
            partialDayJson(DATE, "12:00", "14:00")
        );

        createTimeOffRequest(
            employeeId,
            partialDayJson(DATE, "13:00", "15:00")
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Time off conflicts with an existing absence")
            );
    }

    // TEST 8: Allow back-to-back absences

    @Test
    void shouldAllowNonOverlappingTimeOff() throws Exception {
        Long employeeId = createEmployee();

        createTimeOff(
            employeeId,
            partialDayJson(DATE, "12:00", "14:00")
        );

        createTimeOffRequest(
            employeeId,
            partialDayJson(DATE, "14:00", "16:00")
        )
            .andExpect(status().isCreated());

        mockMvc.perform(get(url(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    // TEST 9: Full-day absence conflicts with partial-day absence

    @Test
    void shouldRejectFullDayConflict() throws Exception {
        Long employeeId = createEmployee();

        createTimeOff(
            employeeId,
            partialDayJson(DATE, "12:00", "14:00")
        );

        createTimeOffRequest(
            employeeId,
            fullDayJson(DATE)
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Time off conflicts with an existing absence")
            );
    }

    // TEST 10: Update an existing absence

    @Test
    void shouldUpdateTimeOff() throws Exception {
        Long employeeId = createEmployee();

        Long timeOffId = createTimeOff(
            employeeId,
            partialDayJson(DATE, "12:00", "14:00")
        );

        mockMvc.perform(
            patch(itemUrl(employeeId, timeOffId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "endTime": "15:00",
                      "reason": "Updated personal time"
                    }
                    """)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(timeOffId))
            .andExpect(
                jsonPath("$.reason").value("Updated personal time")
            );
    }

    // TEST 11: Convert partial-day absence to full-day

    @Test
    void shouldConvertPartialToFullDay() throws Exception {
        Long employeeId = createEmployee();

        Long timeOffId = createTimeOff(
            employeeId,
            partialDayJson(DATE, "12:00", "14:00")
        );

        mockMvc.perform(
            patch(itemUrl(employeeId, timeOffId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"fullDay": true}
                    """)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fullDay").value(true))
            .andExpect(jsonPath("$.startTime").isEmpty())
            .andExpect(jsonPath("$.endTime").isEmpty());
    }

    // TEST 12: Reject updates overlapping another absence

    @Test
    void shouldRejectConflictingUpdate() throws Exception {
        Long employeeId = createEmployee();

        Long firstId = createTimeOff(
            employeeId,
            partialDayJson(DATE, "09:00", "11:00")
        );

        createTimeOff(
            employeeId,
            partialDayJson(DATE, "13:00", "15:00")
        );

        mockMvc.perform(
            patch(itemUrl(employeeId, firstId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"endTime": "14:00"}
                    """)
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Time off conflicts with an existing absence")
            );
    }

    // TEST 13: Reject time off for inactive employees

    @Test
    void shouldRejectInactiveEmployee() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
            patch("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"active": false}
                    """)
        )
            .andExpect(status().isOk());

        createTimeOffRequest(
            employeeId,
            fullDayJson(DATE)
        )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Cannot create time off for an inactive employee")
            );
    }

    // TEST 14: Cannot edit another employee's time off

    @Test
    void shouldRejectDifferentEmployeeTimeOff() throws Exception {
        Long employeeA = createEmployee();
        Long employeeB = createEmployee();

        Long timeOffId = createTimeOff(
            employeeA,
            fullDayJson(DATE)
        );

        mockMvc.perform(
            patch(itemUrl(employeeB, timeOffId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason": "Changed"}
                    """)
        )
            .andExpect(status().isNotFound());
    }

    // TEST 15: Delete an absence

    @Test
    void shouldDeleteTimeOff() throws Exception {
        Long employeeId = createEmployee();

        Long timeOffId = createTimeOff(
            employeeId,
            fullDayJson(DATE)
        );

        mockMvc.perform(
            delete(itemUrl(employeeId, timeOffId))
        )
            .andExpect(status().isNoContent());

        mockMvc.perform(get(url(employeeId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // TEST 16: Missing employee

    @Test
    void shouldReturn404ForMissingEmployee() throws Exception {
        mockMvc.perform(
            get("/api/employees/999999/time-off")
        )
            .andExpect(status().isNotFound());
    }

    // HELPER METHODS

    private String url(Long employeeId) {
        return "/api/employees/" + employeeId + "/time-off";
    }

    private String itemUrl(Long employeeId, Long timeOffId) {
        return url(employeeId) + "/" + timeOffId;
    }

    private ResultActions createTimeOffRequest(
        Long employeeId,
        String json
    ) throws Exception {
        return mockMvc.perform(
            post(url(employeeId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        );
    }

    private Long createEmployee() throws Exception {
        int number = COUNTER.incrementAndGet();

        MvcResult result = mockMvc.perform(
            post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName": "TimeOff",
                      "lastName": "Test%d"
                    }
                    """.formatted(number))
        )
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(result);
    }

    private Long createTimeOff(
        Long employeeId,
        String json
    ) throws Exception {
        MvcResult result = createTimeOffRequest(
            employeeId,
            json
        )
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(result);
    }

    private String fullDayJson(String date) {
        return """
            {
              "timeOffDate": "%s",
              "fullDay": true,
              "reason": "Vacation"
            }
            """.formatted(date);
    }

    private String partialDayJson(
        String date,
        String start,
        String end
    ) {
        return """
            {
              "timeOffDate": "%s",
              "fullDay": false,
              "startTime": "%s",
              "endTime": "%s"
            }
            """.formatted(date, start, end);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return json.get("id").asLong();
    }
}
