package com.pearlnails.backend.servicecatalog.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findAllByOrderByCategoryDisplayOrderAscDisplayOrderAscNameAsc();

    List<Service> findByActiveTrueOrderByCategoryDisplayOrderAscDisplayOrderAscNameAsc();

    List<Service> findByCategoryIdOrderByDisplayOrderAscNameAsc(Long categoryId);

    List<Service> findByCategoryIdAndActiveTrueOrderByDisplayOrderAscNameAsc(
        Long categoryId
    );

    Optional<Service> findByCategoryIdAndNameIgnoreCase(
        Long categoryId,
        String name
    );

    boolean existsByCategoryIdAndNameIgnoreCase(
        Long categoryId,
        String name
    );
}