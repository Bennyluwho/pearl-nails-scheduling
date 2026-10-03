package com.pearlnails.backend.employee;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pearlnails.backend.employee.dto.CreateEmployeeRequest;
import com.pearlnails.backend.employee.dto.EmployeeResponse;
import com.pearlnails.backend.employee.dto.UpdateEmployeeRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;

    public EmployeeController(
        EmployeeService employeeService
    ) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getEmployeeById(
        @PathVariable Long id
    ) {
        return employeeService.getEmployeeById(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
        @Valid @RequestBody CreateEmployeeRequest request
    ) {
        EmployeeResponse created =
            employeeService.createEmployee(request);

        return ResponseEntity
            .created(
                URI.create("/api/employees/" + created.id())
            )
            .body(created);
    }

    @PatchMapping("/{id}")
    public EmployeeResponse updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return employeeService.updateEmployee(id,
             request
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateEmployee(
        @PathVariable Long id
    ) {
        employeeService.deactivateEmployee(id);

        return ResponseEntity.noContent().build();
    }
}