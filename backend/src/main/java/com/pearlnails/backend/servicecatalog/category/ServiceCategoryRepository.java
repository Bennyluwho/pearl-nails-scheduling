package com.pearlnails.backend.servicecatalog.category;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCategoryRepository 
    extends JpaRepository<ServiceCategory, Long> {
    
    List<ServiceCategory> findAllByOrderByDisplayOrderAscNameAsc();

    List<ServiceCategory> findByActiveTrueOrderByDisplayOrderAscNameAsc();
    
    Optional<ServiceCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}