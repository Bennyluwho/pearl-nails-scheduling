package com.pearlnails.backend.scheduling.businesshours;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.common.exception.InvalidOperationException;
import com.pearlnails.backend.common.exception.ResourceNotFoundException;

import com.pearlnails.backend.scheduling.businesshours.dto.BusinessHoursResponse;
import com.pearlnails.backend.scheduling.businesshours.dto.UpdateBusinessHoursRequest;

import com.pearlnails.backend.scheduling.workinghours.EmployeeWorkingHours;
import com.pearlnails.backend.scheduling.workinghours.EmployeeWorkingHoursRepository;

@Service 
public class BusinessHoursService {
    
    private final BusinessHoursRepository businessHoursRepository;
    private final EmployeeWorkingHoursRepository workingHoursRepository;

    public BusinessHoursService(
        BusinessHoursRepository businessHoursRepository,
        EmployeeWorkingHoursRepository workingHoursRepository
    ) {
        this.businessHoursRepository = businessHoursRepository;
        this.workingHoursRepository = workingHoursRepository;
    }

    @Transactional(readOnly = true)
    public List<BusinessHoursResponse> getAllBusinessHours() {
        return businessHoursRepository
            .findAllByOrderByDayOfWeekAsc()
            .stream()
            .map(BusinessHoursResponse::from)
            .toList();
    }

    @Transactional
    public BusinessHoursResponse updateBusinessHours(
        Integer dayOfWeek,
        UpdateBusinessHoursRequest request
    ) {
        if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
            throw new InvalidOperationException(
                "Day of week must be between 1 and 7"
            );
        }

        BusinessHours hours = businessHoursRepository
            .findByDayOfWeek(dayOfWeek)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Business hours not found for day: " + dayOfWeek
                )
            );
        
        List<EmployeeWorkingHours> employeeShifts = 
            workingHoursRepository.findAllByDayOfWeek(dayOfWeek);
        
        if (Boolean.TRUE.equals(request.closed())) {

            if (
                request.openTime() != null ||
                request.closeTime() != null
            ) {
                throw new InvalidOperationException(
                    "Closed days cannot have opening or closing times"
                );
            }

            if (!employeeShifts.isEmpty()) {
                throw new InvalidOperationException(
                  "Cannot close salon while employees have scheduled shifts"  
                );
            }

            hours.setClosed(true);
            hours.setOpenTime(null);
            hours.setCloseTime(null);

        } else {
            if (
                request.openTime() == null ||
                request.closeTime() == null
            ) {
                throw new InvalidOperationException(
                    "Opening and closing times are required"
                );
            }

            if (!request.openTime().isBefore(request.closeTime())) {
                throw new InvalidOperationException(
                    "Opening time must be before closing time"
                );
            }

            for (EmployeeWorkingHours shift : employeeShifts) {

                boolean outsideHours =
                    shift.getStartTime().isBefore(request.openTime()) ||
                    shift.getEndTime().isAfter(request.closeTime());
                
                if (outsideHours) {
                    throw new InvalidOperationException(
                        "New business hours conflict with employee shifts"
                    );
                }
            }

            hours.setClosed(false);
            hours.setOpenTime(request.openTime());
            hours.setCloseTime(request.closeTime());
        }

        return BusinessHoursResponse.from(hours);
    }
}
