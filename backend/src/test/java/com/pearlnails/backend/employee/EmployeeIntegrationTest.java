package com.pearlnails.backend.employee;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EmployeeIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404ForMissingEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateEmployee() throws Exception {
        mockMvc.perform(
                post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "firstName": "Test",
                          "lastName": "Employee",
                          "displayName": "Tester",
                          "email": "test@example.com",
                          "displayOrder": 10
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Test"))
            .andExpect(jsonPath("$.lastName").value("Employee"))
            .andExpect(jsonPath("$.displayName").value("Tester"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectInvalidEmployee() throws Exception {
        mockMvc.perform(
                post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "firstName": "",
                          "lastName": "",
                          "email": "not-an-email"
                        }
                        """)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                patch("/api/employees/" + employeeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "displayName": "Updated Employee",
                          "displayOrder": 25
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.displayName")
                    .value("Updated Employee")
            )
            .andExpect(
                jsonPath("$.displayOrder")
                    .value(25)
            );
    }

    @Test
    void shouldDeactivateEmployee() throws Exception {
        Long employeeId = createEmployee();

        mockMvc.perform(
                delete("/api/employees/" + employeeId)
            )
            .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/employees/" + employeeId)
            )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath("$.active").value(false)
            );
    }

    private Long createEmployee() throws Exception {
        MvcResult result = mockMvc.perform(
                post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "firstName": "Integration",
                          "lastName": "Test",
                          "displayName": "Integration Test"
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }
}
