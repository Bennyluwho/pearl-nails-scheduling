package com.pearlnails.backend.scheduling.businesshours;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Long> {
    List<BusinessHours> findAllByOrderByDayOfWeekAsc();

    Optional<BusinessHours> findByDayOfWeek(
        Short dayOfWeek
    );
}
