package com.pearlnails.backend.employeeassignment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeServiceAssignmentRepository
    extends JpaRepository<
        EmployeeServiceAssignment,
        EmployeeServiceAssignmentId
    > {

    List<EmployeeServiceAssignment> findAllByEmployee_Id(
        Long employeeId
    );

    List<EmployeeServiceAssignment> findAllByService_Id(
        Long serviceId
    );

    boolean existsByEmployee_IdAndService_Id(
        Long employeeId,
        Long serviceId
    );

    Optional<EmployeeServiceAssignment>
        findByEmployee_IdAndService_Id(
            Long employeeId,
            Long serviceId
        );
}