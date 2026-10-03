package com.pearlnails.backend.employeeassignment;

import java.time.OffsetDateTime;

import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.servicecatalog.service.Service;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_services")
public class EmployeeServiceAssignment {
    
    @EmbeddedId
    private EmployeeServiceAssignmentId id;

    @MapsId("employeeId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @MapsId("serviceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(
        name = "created_at", 
        nullable = false,
        insertable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    protected EmployeeServiceAssignment() {
    }

    public EmployeeServiceAssignment(
        Employee employee, 
        Service service
    ) {
        this.employee = employee;
        this.service = service;
        this.id = new EmployeeServiceAssignmentId(
            employee.getId(), 
            service.getId()
        );
    }

    public EmployeeServiceAssignmentId getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Service getService() {
        return service;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}