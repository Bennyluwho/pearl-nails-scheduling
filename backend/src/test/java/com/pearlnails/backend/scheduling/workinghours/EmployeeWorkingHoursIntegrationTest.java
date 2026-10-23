
package com.pearlnails.backend.scheduling.workinghours;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EmployeeWorkingHoursIntegrationTest {

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

    // TEST 1: GET employee working hours

    @Test
    void shouldGetEmployeeWorkingHours() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                get("/api/employees/{id}/working-hours", employeeId)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // TEST 2: Create a valid shift

    @Test
    void shouldCreateWorkingHours() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(1, "09:00", "13:00"))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.employeeId").value(employeeId))
            .andExpect(jsonPath("$.dayOfWeek").value(1));

        mockMvc.perform(
                get("/api/employees/{id}/working-hours", employeeId)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].dayOfWeek").value(1));
    }

    // TEST 3: Allow split shifts

    @Test
    void shouldAllowNonOverlappingShifts() throws Exception {
        Long employeeId = createEmployee();

        createShift(employeeId, 1, "09:00", "13:00");

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(1, "14:00", "18:00"))
            )
            .andExpect(status().isCreated());
    }

    // TEST 4: Reject overlapping shifts

    @Test
    void shouldRejectOverlappingShifts() throws Exception {
        Long employeeId = createEmployee();

        createShift(employeeId, 1, "09:00", "13:00");

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(1, "12:00", "17:00"))
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Working hours overlap an existing shift")
            );
    }

    // TEST 5: Reject shifts outside salon hours

    @Test
    void shouldRejectShiftOutsideSalonHours() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(1, "08:00", "12:00"))
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Employee shift must be within salon business hours")
            );
    }

    // TEST 6: Reject closed days

    @Test
    void shouldRejectClosedSalonDay() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(7, "09:00", "17:00"))
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Salon is closed on this day")
            );
    }

    // TEST 7: Reject invalid time ranges

    @Test
    void shouldRejectInvalidTimeRange() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(1, "17:00", "09:00"))
            )
            .andExpect(status().isBadRequest());
    }

    // TEST 8: Reject invalid weekday

    @Test
    void shouldRejectInvalidDayOfWeek() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(8, "09:00", "17:00"))
            )
            .andExpect(status().isBadRequest());
    }

    // TEST 9: Reject inactive employees

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

        mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(1, "09:00", "17:00"))
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Cannot schedule an inactive employee")
            );
    }

    // TEST 10: Update an existing shift

    @Test
    void shouldUpdateWorkingHours() throws Exception {
        Long employeeId = createEmployee();

        Long shiftId =
            createShift(employeeId, 1, "09:00", "13:00");

        mockMvc.perform(
                patch(
                    "/api/employees/{id}/working-hours/{shiftId}",
                    employeeId,
                    shiftId
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"endTime": "17:00"}
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(shiftId));
    }

    // TEST 11: Reject overlapping updates

    @Test
    void shouldRejectOverlappingUpdate() throws Exception {
        Long employeeId = createEmployee();

        Long firstShift =
            createShift(employeeId, 1, "09:00", "13:00");

        createShift(employeeId, 1, "14:00", "18:00");

        mockMvc.perform(
                patch(
                    "/api/employees/{id}/working-hours/{shiftId}",
                    employeeId,
                    firstShift
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"endTime": "16:00"}
                    """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value("Working hours overlap an existing shift")
            );
    }

    // TEST 12: Delete an existing shift

    @Test
    void shouldDeleteWorkingHours() throws Exception {
        Long employeeId = createEmployee();

        Long shiftId =
            createShift(employeeId, 1, "09:00", "13:00");

        mockMvc.perform(
                delete(
                    "/api/employees/{id}/working-hours/{shiftId}",
                    employeeId,
                    shiftId
                )
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/employees/{id}/working-hours", employeeId)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    // TEST 13: Prevent one employee editing another's shift

    @Test
    void shouldRejectShiftOwnedByAnotherEmployee() throws Exception {
        Long employeeA = createEmployee();
        Long employeeB = createEmployee();

        Long shiftId =
            createShift(employeeA, 1, "09:00", "13:00");

        mockMvc.perform(
                patch(
                    "/api/employees/{id}/working-hours/{shiftId}",
                    employeeB,
                    shiftId
                )
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"endTime": "17:00"}
                    """)
            )
            .andExpect(status().isNotFound());
    }

    // TEST 14: Missing employee

    @Test
    void shouldReturn404ForMissingEmployee() throws Exception {
        mockMvc.perform(
                get("/api/employees/999999/working-hours")
            )
            .andExpect(status().isNotFound());
    }

    // HELPERS: Create independent test records

    private Long createEmployee() throws Exception {
        int number = COUNTER.incrementAndGet();

        MvcResult result = mockMvc.perform(
                post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "firstName": "Schedule",
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
        int day,
        String start,
        String end
    ) throws Exception {

        MvcResult result = mockMvc.perform(
                post("/api/employees/{id}/working-hours", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(shiftJson(day, start, end))
            )
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(result);
    }

    private String shiftJson(
        int day,
        String start,
        String end
    ) {
        return """
            {
              "dayOfWeek": %d,
              "startTime": "%s",
              "endTime": "%s"
            }
            """.formatted(day, start, end);
    }

    private Long extractId(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }
}
