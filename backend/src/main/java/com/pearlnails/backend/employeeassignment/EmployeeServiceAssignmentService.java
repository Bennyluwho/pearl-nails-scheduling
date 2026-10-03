package com.pearlnails.backend.employeeassignment;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.common.exception.DuplicateResourceException;
import com.pearlnails.backend.common.exception.InvalidOperationException;
import com.pearlnails.backend.common.exception.ResourceNotFoundException;
import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.employee.EmployeeRepository;
import com.pearlnails.backend.servicecatalog.service.ServiceRepository;
import com.pearlnails.backend.servicecatalog.service.dto.ServiceResponse;

@Service
public class EmployeeServiceAssignmentService {

    private final EmployeeServiceAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;

    public EmployeeServiceAssignmentService(
        EmployeeServiceAssignmentRepository assignmentRepository,
        EmployeeRepository employeeRepository,
        ServiceRepository serviceRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesForEmployee(
        Long employeeId
    ) {
        findEmployee(employeeId);

        return assignmentRepository
            .findAllByEmployee_Id(employeeId)
            .stream()
            .map(assignment ->
                ServiceResponse.from(
                    assignment.getService()
                )
            )
            .sorted(
                Comparator
                    .comparing(ServiceResponse::categoryName)
                    .thenComparing(ServiceResponse::displayOrder)
                    .thenComparing(ServiceResponse::name)
            )
            .toList();
    }

    @Transactional
    public ServiceResponse assignService(
        Long employeeId,
        Long serviceId
    ) {
        Employee employee = findEmployee(employeeId);

        com.pearlnails.backend.servicecatalog.service.Service
            salonService = findService(serviceId);

        if (!employee.getActive()) {
            throw new InvalidOperationException(
                "Cannot assign services to an inactive employee"
            );
        }

        if (!salonService.getActive()) {
            throw new InvalidOperationException(
                "Cannot assign an inactive service to an employee"
            );
        }

        if (
            assignmentRepository
                .existsByEmployee_IdAndService_Id(
                    employeeId,
                    serviceId
                )
        ) {
            throw new DuplicateResourceException(
                "Employee is already assigned to this service"
            );
        }

        EmployeeServiceAssignment assignment =
            new EmployeeServiceAssignment(
                employee,
                salonService
            );

        assignmentRepository.save(assignment);

        return ServiceResponse.from(salonService);
    }

    @Transactional
    public void removeService(
        Long employeeId,
        Long serviceId
    ) {
        EmployeeServiceAssignment assignment =
            assignmentRepository
                .findByEmployee_IdAndService_Id(
                    employeeId,
                    serviceId
                )
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Employee service assignment not found"
                    )
                );

        assignmentRepository.delete(assignment);
    }

    @Transactional(readOnly = true)
    public boolean isAssigned(
        Long employeeId,
        Long serviceId
    ) {
        return assignmentRepository
            .existsByEmployee_IdAndService_Id(
                employeeId,
                serviceId
            );
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository
            .findById(employeeId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee not found: " + employeeId
                )
            );
    }

    private com.pearlnails.backend.servicecatalog.service.Service
        findService(Long serviceId) {

        return serviceRepository
            .findById(serviceId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Service not found: " + serviceId
                )
            );
    }
}