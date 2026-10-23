package com.pearlnails.backend.scheduling.workinghours;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.pearlnails.backend.scheduling.workinghours.dto.CreateEmployeeWorkingHoursRequest;
import com.pearlnails.backend.scheduling.workinghours.dto.UpdateEmployeeWorkingHoursRequest;
import com.pearlnails.backend.scheduling.workinghours.dto.EmployeeWorkingHoursResponse;

@RestController
@RequestMapping("api/employees/{employeeId}/working-hours")
public class EmployeeWorkingHoursController {
    
    private final EmployeeWorkingHoursService workingHoursService;

    public EmployeeWorkingHoursController(
        EmployeeWorkingHoursService workingHoursService
    ) {
        this.workingHoursService = workingHoursService;
    }

    @GetMapping 
    public List<EmployeeWorkingHoursResponse> getWorkingHours(
        @PathVariable Long employeeId
    ) {
        return workingHoursService.getWorkingHours(employeeId);
    }

    @PostMapping
    public ResponseEntity<EmployeeWorkingHoursResponse> createWorkingHours (
        @PathVariable Long employeeId,
        @Valid @RequestBody CreateEmployeeWorkingHoursRequest request
    ) {
        EmployeeWorkingHoursResponse created =
            workingHoursService.createWorkingHours(
                employeeId,
                request
            );
        
            URI location = URI.create(
              "/api/employees/" + employeeId
                + "/working-hours/" + created.id()  
            );

            return ResponseEntity
                .created(location)
                .body(created);
    }

    @PatchMapping("/{shiftId}")
    public EmployeeWorkingHoursResponse updateWorkingHours(
        @PathVariable Long employeeId,
        @PathVariable Long shiftId,
        @Valid @RequestBody UpdateEmployeeWorkingHoursRequest request
    ) {
        return workingHoursService.updateWorkingHours(
            employeeId,
            shiftId,
            request
        );
    }

    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Void> deleteWorkingHours(
        @PathVariable Long employeeId,
        @PathVariable Long shiftId
    ) {
        workingHoursService.deleteWorkingHours(
            employeeId,
            shiftId
        );

        return ResponseEntity.noContent().build();
    }
}
