package com.pearlnails.backend.scheduling.timeoff;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.pearlnails.backend.scheduling.timeoff.dto.CreateEmployeeTimeOffRequest;
import com.pearlnails.backend.scheduling.timeoff.dto.UpdateEmployeeTimeOffRequest;
import com.pearlnails.backend.scheduling.timeoff.dto.EmployeeTimeOffResponse;

@RestController
@RequestMapping("/api/employees/{employeeId}/time-off")
public class EmployeeTimeOffController {
    
    private final EmployeeTimeOffService timeOffService;

    public EmployeeTimeOffController(
        EmployeeTimeOffService timeOffService
    ) {
        this.timeOffService = timeOffService;
    }

    @GetMapping
    public List<EmployeeTimeOffResponse> getTimeOff(
        @PathVariable Long employeeId
    ) {
        return timeOffService.getTimeOff(employeeId);
    }

    @PostMapping
    public ResponseEntity<EmployeeTimeOffResponse> createTimeOff(
        @PathVariable Long employeeId,
        @Valid @RequestBody CreateEmployeeTimeOffRequest request
    ) {
        EmployeeTimeOffResponse created =
            timeOffService.createTimeOff(employeeId, request);
        
        URI location = URI.create(
            "/api/employees/" + employeeId
                + "/time-off/" + created.id()
        );

        return ResponseEntity
            .created(location)
            .body(created);
    }

    @PatchMapping("/{timeOffId}")
    public EmployeeTimeOffResponse updateTimeOff(
        @PathVariable Long employeeId,
        @PathVariable Long timeOffId,
        @Valid @RequestBody UpdateEmployeeTimeOffRequest request
    ) {
        return timeOffService.updateTimeOff(employeeId,
            timeOffId,
            request
        );
    }

    @DeleteMapping("/{timeOffId}")
    public ResponseEntity<Void> deleteTimeOff(
        @PathVariable Long employeeId,
        @PathVariable Long timeOffId
    ) {
        timeOffService.deleteTimeOff(employeeId,
            timeOffId
        );

        return ResponseEntity.noContent().build();
    }
}
