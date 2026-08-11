package com.pearlnails.backend.servicecatalog;

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
class ServiceCatalogIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSeededServices() throws Exception {
        mockMvc.perform(get("/api/services"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn404WhenServiceDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/services/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateService() throws Exception {
        mockMvc.perform(
                post("/api/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": 1,
                          "name": "Create Test Service",
                          "description": "Created by integration test",
                          "price": 45.00,
                          "durationMinutes": 45,
                          "bufferMinutes": 5,
                          "displayOrder": 100
                        }
                        """)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Create Test Service"))
            .andExpect(jsonPath("$.price").value(45.00))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectInvalidService() throws Exception {
        mockMvc.perform(
                post("/api/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": 1,
                          "name": "",
                          "price": -10.00,
                          "durationMinutes": 0,
                          "bufferMinutes": -1
                        }
                        """)
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.validationErrors.name").exists())
            .andExpect(jsonPath("$.validationErrors.price").exists())
            .andExpect(jsonPath("$.validationErrors.durationMinutes").exists())
            .andExpect(jsonPath("$.validationErrors.bufferMinutes").exists());
    }

    @Test
    void shouldRejectDuplicateServiceInSameCategory() throws Exception {
        String request = """
            {
              "categoryId": 1,
              "name": "Duplicate Test Service",
              "price": 40.00,
              "durationMinutes": 30,
              "bufferMinutes": 0,
              "displayOrder": 100
            }
            """;

        mockMvc.perform(
                post("/api/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void shouldPatchService() throws Exception {
        Long serviceId = createTestService("Patch Test Service");

        mockMvc.perform(
                patch("/api/services/" + serviceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "price": 99.00,
                          "durationMinutes": 75
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.price").value(99.00))
            .andExpect(jsonPath("$.durationMinutes").value(75));
    }

    @Test
    void shouldSoftDeleteService() throws Exception {
        Long serviceId = createTestService("Delete Test Service");

        mockMvc.perform(delete("/api/services/" + serviceId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/services/" + serviceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }

    private Long createTestService(String name) throws Exception {
        MvcResult result = mockMvc.perform(
                post("/api/services")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "categoryId": 1,
                          "name": "%s",
                          "description": "Integration test service",
                          "price": 50.00,
                          "durationMinutes": 45,
                          "bufferMinutes": 5,
                          "displayOrder": 100
                        }
                        """.formatted(name))
            )
            .andExpect(status().isCreated())
            .andReturn();

        JsonNode response = objectMapper.readTree(
            result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }
}
