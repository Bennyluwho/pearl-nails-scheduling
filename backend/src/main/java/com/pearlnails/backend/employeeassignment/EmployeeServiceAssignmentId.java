package com.pearlnails.backend.employeeassignment;

import java.io.Serializable;
import  java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class EmployeeServiceAssignmentId implements Serializable {

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "service_id")
    private Long serviceId;

    public EmployeeServiceAssignmentId() {
    }

    public EmployeeServiceAssignmentId(
        Long employeeId, 
        Long serviceId
    ) {
        this.employeeId = employeeId;
        this.serviceId = serviceId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;

        if (!(object instanceof EmployeeServiceAssignmentId)) return false;

        EmployeeServiceAssignmentId that = (EmployeeServiceAssignmentId) object;
        
        return Objects.equals(getEmployeeId(), that.getEmployeeId()) &&
                Objects.equals(getServiceId(), that.getServiceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmployeeId(), getServiceId());
    }
}