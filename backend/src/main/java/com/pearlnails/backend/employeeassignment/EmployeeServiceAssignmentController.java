package com.pearlnails.backend.employeeassignment;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pearlnails.backend.servicecatalog.service.dto.ServiceResponse;

@RestController
@RequestMapping("/api/employees/{employeeId}/services")
public class EmployeeServiceAssignmentController {
    
    private final EmployeeServiceAssignmentService assignmentService;

    public EmployeeServiceAssignmentController (
        EmployeeServiceAssignmentService assignmentService
    ) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public List<ServiceResponse> getServicesForEmployee (
        @PathVariable Long employeeId
    ) {
        return assignmentService.getServicesForEmployee(
            employeeId
        );
    }

    @PostMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> assignService(
        @PathVariable Long employeeId,
        @PathVariable Long serviceId
    ) {
        ServiceResponse assigned =
            assignmentService.assignService(
                employeeId,
                serviceId
            );
        URI location = URI.create(
            "/api/employees/"
                + employeeId
                + "/services/"
                + serviceId
        );

        return ResponseEntity
            .created(location)
            .body(assigned);
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> removeService(
        @PathVariable Long employeeId,
        @PathVariable Long serviceId
    ) {
        assignmentService.removeService(
            employeeId, 
            serviceId
        );

        return ResponseEntity.noContent().build();
    }
}