package com.pearlnails.backend.employee;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.common.exception.ResourceNotFoundException;
import com.pearlnails.backend.employee.dto.CreateEmployeeRequest;
import com.pearlnails.backend.employee.dto.EmployeeResponse;
import com.pearlnails.backend.employee.dto.UpdateEmployeeRequest;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(
        EmployeeRepository employeeRepository
    ) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository
            .findAllByOrderByDisplayOrderAscFirstNameAsc()
            .stream()
            .map(EmployeeResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return EmployeeResponse.from(
            findEmployeeById(id)
        );
    }

    @Transactional
    public EmployeeResponse createEmployee(
        CreateEmployeeRequest request
    ) {
        int displayOrder = 
            request.displayOrder() != null
                ? request.displayOrder()
                : 0;

        Employee employee = new Employee(
            request.firstName().trim(),
            request.lastName().trim(),
            normalizeNullable(request.displayName()),
            normalizeNullable(request.phone()),
            normalizeNullable(request.email()),
            displayOrder
        );

        Employee saved = 
            employeeRepository.save(employee);

        return EmployeeResponse.from(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(
        Long id,
        UpdateEmployeeRequest request
    ) {
        Employee employee = findEmployeeById(id);

        if (request.firstName() != null) {
            employee.setFirstName(
                request.firstName().trim()
            );
        }

        if (request.lastName() != null) {
            employee.setLastName(
                request.lastName().trim()
            );
        }
    
        if (request.displayName() != null) {
            employee.setDisplayName(
                normalizeNullable(request.displayName())
            );
        }

        if (request.phone() != null) {
            employee.setPhone(
                normalizeNullable(request.phone())
            );
        }
        
        if (request.email() != null) {
            employee.setEmail(
                normalizeNullable(request.email())
            );
        }

        if (request.displayOrder() != null) {
            employee.setDisplayOrder(
                request.displayOrder()
            );
        }

        if (request.active() != null) {
            employee.setActive(
                request.active()
            );
        }

        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = findEmployeeById(id);
        employee.setActive(false);
    }

    private Employee findEmployeeById(Long id) {
        return employeeRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee not found: " + id
                ) 
            );
    }

    private String normalizeNullable(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
