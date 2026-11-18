package com.pearlnails.backend.appointment;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {
        
        // All appointments belonging to one employee

        List<Appointment>
            findAllByEmployee_IdOrderByStartsAtDesc(
                Long employeeId
            );
        
        // Appointments beginning with a selected date range

        List<Appointment>
            findAllByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                OffsetDateTime windowStart,
                OffsetDateTime windowEnd
            );

        // Non-cancelled appointments overlapping a time window

        List<Appointment>
            findAllByEmployee_IdAndStartsAtLessThanAndReservedUntilGreaterThanAndStatusNotOrderByStartsAtAsc(
                Long employeeId,
                OffsetDateTime windowEnd,
                OffsetDateTime windowStart,
                AppointmentStatus excludedStatus
            );
}    

