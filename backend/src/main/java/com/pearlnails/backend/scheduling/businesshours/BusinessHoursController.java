package com.pearlnails.backend.scheduling.businesshours;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.pearlnails.backend.scheduling.businesshours.dto.BusinessHoursResponse;
import com.pearlnails.backend.scheduling.businesshours.dto.UpdateBusinessHoursRequest;

@RestController
@RequestMapping("/api/business-hours") 
public class BusinessHoursController {

    private final BusinessHoursService businessHoursService;

    public BusinessHoursController(
        BusinessHoursService businessHoursService
    ) {
        this.businessHoursService = businessHoursService;
    }

    @GetMapping 
    public List<BusinessHoursResponse> getBusinessHours() {
        return businessHoursService.getAllBusinessHours();
    }

    @PutMapping("/{dayOfWeek}")
    public BusinessHoursResponse updateBusinessHours(
        @PathVariable Integer dayOfWeek,
        @Valid @RequestBody UpdateBusinessHoursRequest request
    ) {
        return businessHoursService.updateBusinessHours(
            dayOfWeek, 
            request
        );
    }
}
