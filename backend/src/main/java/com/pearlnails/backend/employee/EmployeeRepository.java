package com.pearlnails.backend.employee;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository 
    extends JpaRepository<Employee, Long> {
        
    List<Employee> findAllByOrderByDisplayOrderAscFirstNameAsc();
    
    List<Employee> findByActiveTrueOrderByDisplayOrderAscFirstNameAsc();
}
    
