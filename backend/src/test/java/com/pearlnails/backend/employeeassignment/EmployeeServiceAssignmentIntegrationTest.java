package com.pearlnails.backend.employeeassignment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class EmployeeServiceAssignmentIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    private static final AtomicInteger COUNTER =
        new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldAssignServiceToEmployee() throws Exception {
        Long employeeId = createEmployee();
        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(serviceId))
            .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(
                get(
                    "/api/employees/{employeeId}/services",
                    employeeId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(serviceId));
    }

    @Test
    void shouldRejectDuplicateAssignment() throws Exception {
        Long employeeId = createEmployee();
        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        assignService(employeeId, serviceId);

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isConflict())
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Employee is already assigned to this service"
                    )
            );
    }

    @Test
    void shouldRemoveServiceFromEmployee() throws Exception {
        Long employeeId = createEmployee();
        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        assignService(employeeId, serviceId);

        mockMvc.perform(
                delete(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get(
                    "/api/employees/{employeeId}/services",
                    employeeId
                )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturn404WhenRemovingMissingAssignment()
        throws Exception {

        Long employeeId = createEmployee();
        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        mockMvc.perform(
                delete(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForMissingEmployee()
        throws Exception {

        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    999999L,
                    serviceId
                )
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404ForMissingService()
        throws Exception {

        Long employeeId = createEmployee();

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    999999L
                )
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectAssignmentToInactiveEmployee()
        throws Exception {

        Long employeeId = createEmployee();
        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        mockMvc.perform(
                patch("/api/employees/{id}", employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "active": false
                        }
                        """)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Cannot assign services to an inactive employee"
                    )
            );
    }

    @Test
    void shouldRejectInactiveService()
        throws Exception {

        Long employeeId = createEmployee();
        Long categoryId = createCategory();
        Long serviceId = createService(categoryId);

        mockMvc.perform(
                patch("/api/services/{id}", serviceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "active": false
                        }
                        """)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isBadRequest())
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Cannot assign an inactive service to an employee"
                    )
            );
    }

    private Long createEmployee() throws Exception {
        int number = COUNTER.incrementAndGet();

        MvcResult result = mockMvc.perform(
                post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "firstName": "Assignment",
                          "lastName": "Test%d",
                          "displayName": "Assignment Test %d"
                        }
                        """.formatted(number, number))
            )
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(result);
    }

    private Long createCategory() throws Exception {
        int number = COUNTER.incrementAndGet();

        MvcResult result = mockMvc.perform(
                post("/api/service-categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "name": "Assignment Category %d",
                          "displayOrder": %d
                        }
                        """.formatted(number, number))
            )
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(result);
    }

    private Long createService(Long categoryId)
        throws Exception {

        int number = COUNTER.incrementAndGet();

        MvcResult result = mockMvc.perform(
                post("/api/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": %d,
                          "name": "Assignment Service %d",
                          "price": 25.00,
                          "durationMinutes": 30,
                          "bufferMinutes": 0,
                          "displayOrder": %d
                        }
                        """.formatted(
                            categoryId,
                            number,
                            number
                        ))
            )
            .andExpect(status().isCreated())
            .andReturn();

        return extractId(result);
    }

    private void assignService(
        Long employeeId,
        Long serviceId
    ) throws Exception {

        mockMvc.perform(
                post(
                    "/api/employees/{employeeId}/services/{serviceId}",
                    employeeId,
                    serviceId
                )
            )
            .andExpect(status().isCreated());
    }

    private Long extractId(MvcResult result)
        throws Exception {

        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }
}