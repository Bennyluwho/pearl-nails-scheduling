
package com.pearlnails.backend.scheduling.timeoff;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import com.pearlnails.backend.employee.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_time_off")
public class EmployeeTimeOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "employee_id",
        nullable = false
    )
    private Employee employee;

    @Column(
        name = "time_off_date",
        nullable = false
    )
    private LocalDate timeOffDate;

    @Column(
        name = "full_day",
        nullable = false
    )
    private Boolean fullDay = false;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(length = 255)
    private String reason;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false,
        insertable = false
    )
    private OffsetDateTime updatedAt;

    protected EmployeeTimeOff() {
    }

    public EmployeeTimeOff(
        Employee employee,
        LocalDate timeOffDate,
        Boolean fullDay,
        LocalTime startTime,
        LocalTime endTime,
        String reason
    ) {
        this.employee = employee;
        this.timeOffDate = timeOffDate;
        this.fullDay = fullDay;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getTimeOffDate() {
        return timeOffDate;
    }

    public void setTimeOffDate(LocalDate timeOffDate) {
        this.timeOffDate = timeOffDate;
    }

    public Boolean getFullDay() {
        return fullDay;
    }

    public void setFullDay(Boolean fullDay) {
        this.fullDay = fullDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}