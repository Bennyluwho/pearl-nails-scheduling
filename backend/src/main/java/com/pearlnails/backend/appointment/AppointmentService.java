package com.pearlnails.backend.appointment;

import java.sql.SQLException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.appointment.dto.AppointmentResponse;
import com.pearlnails.backend.appointment.dto.CreateAppointmentRequest;

import com.pearlnails.backend.common.exception.DuplicateResourceException;
import com.pearlnails.backend.common.exception.InvalidOperationException;
import com.pearlnails.backend.common.exception.ResourceNotFoundException;

import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.employee.EmployeeRepository;

import com.pearlnails.backend.servicecatalog.service.ServiceRepository;

@Service 
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final AppointmentAvailabilityService availabilityService;
    private final EmployeeRepository employeeRepository;
    private final ServiceRepository serviceRepository;

    public AppointmentService(
        AppointmentRepository appointmentRepository,
        AppointmentAvailabilityService availabilityService,
        EmployeeRepository employeeRepository,
        ServiceRepository serviceRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityService = availabilityService;
        this.employeeRepository = employeeRepository;
        this.serviceRepository = serviceRepository;
    }

    // CREATE: Book a confirmed appointment

    @Transactional
    public AppointmentResponse createAppointment(
        CreateAppointmentRequest request
    ) {
        String customerName = normalize(request.customerName());
        String customerPhone = normalize(request.customerPhone());
        String customerEmail = normalize(request.customerEmail());

        if (customerName == null || customerName.length() > 150) {
            throw new InvalidOperationException(
                "Customer name is required and must be at most 150 characters"
            );
        }

        if (customerPhone != null && customerPhone.length() > 30) {
            throw new InvalidOperationException(
              "Customer phone must be at most 30 characters"  
            );
        }

        if (customerEmail != null && customerEmail.length() > 255) {
            throw new InvalidOperationException(
              "Customer email must be at most 255 characters"  
            );
        }

        if (customerPhone == null && customerEmail == null) {
            throw new InvalidOperationException(
                "Customer phone or email is required"
            );
        }

        AppointmentAvailabilityService.AvailableSlot slot = 
            availabilityService.validateAvailability(
                request.employeeId(),
                request.serviceId(),
                request.appointmentDate(),
                request.startTime()
            );

        Employee employee = employeeRepository
            .findById(request.employeeId())
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Employee not found: " + request.employeeId()
                )
            );

        com.pearlnails.backend.servicecatalog.service.Service 
            salonService = serviceRepository
                .findById(request.serviceId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Service not found: " + request.serviceId()
                    )
                );
        
        Appointment appointment = new Appointment(
            employee,
            salonService,

            customerName,
            customerPhone,
            customerEmail,

            slot.startsAt(),
            slot.endsAt(),
            slot.reservedUntil(),

            salonService.getName(),
            salonService.getPrice(),
            salonService.getDurationMinutes(),
            salonService.getBufferMinutes()
        );

        try {
            Appointment saved =
                appointmentRepository.saveAndFlush(appointment);
            
            return AppointmentResponse.from(saved);

        } catch (DataIntegrityViolationException exception) {

            if (isAppointmentOverlap(exception)) {
                throw new DuplicateResourceException(
                    "This appointment time is no longer available"
                );
            }

            throw exception;
        }
    }

    // HELPERS
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private boolean isAppointmentOverlap(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof SQLException sqlException) {
                 if ("23P01".equals(sqlException.getSQLState())) {
                    return true;
                }
            }

            current = current.getCause();

        }

        return false;
    }
}
