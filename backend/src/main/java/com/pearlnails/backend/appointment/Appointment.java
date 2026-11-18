package com.pearlnails.backend.appointment;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.pearlnails.backend.employee.Employee;
import com.pearlnails.backend.servicecatalog.service.Service;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EMPLOYEE AND SERVICE RELATIONSHIPS

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "employee_id",
        nullable = false
    )
    private Employee employee;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "service_id",
        nullable = false
    )
    private Service service;

    // CUSTOMER INFORMATION
    
    @Column(
        name = "customer_name",
        nullable = false,
        length = 150
    )
    private String customerName;

    @Column(name = "customer_phone", length = 30)
    private String customerPhone;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Column(name = "reserved_until", nullable = false)
    private OffsetDateTime reservedUntil;

    @Column(
        name = "service_name",
        nullable = false,
        length = 150
    )
    private String serviceName;

    @Column(
        name = "price_at_booking",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal priceAtBooking;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "buffer_minutes", nullable = false)
    private Integer bufferMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status =
        AppointmentStatus.CONFIRMED;
    
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
        insertable = false,
        updatable = false
    )
    private OffsetDateTime updatedAt;

    protected Appointment() {
    }

    public Appointment(
        Employee employee,
        Service service,
        String customerName,
        String customerPhone,
        String customerEmail,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        OffsetDateTime reservedUntil,
        String serviceName,
        BigDecimal priceAtBooking,
        Integer durationMinutes,
        Integer bufferMinutes
    ) {
        this.employee = employee;
        this.service = service;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.reservedUntil = reservedUntil;
        this.serviceName = serviceName;
        this.priceAtBooking = priceAtBooking;
        this.durationMinutes = durationMinutes;
        this.bufferMinutes = bufferMinutes;
    }

    // GETTERS

    public Long getId() {
        return id;
    }

    public Employee getEmployed() {
        return employee;
    }

    public Service getService() {
        return service;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OffsetDateTime getStartsAt() {
        return startsAt;
    }

    public OffsetDateTime getEndsAt() {
        return endsAt;
    }

    public OffsetDateTime getReservedUntil() {
        return reservedUntil;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal getPriceAtBooking() {
        return priceAtBooking;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getBufferMinutes() {
        return bufferMinutes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // SETTERS

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public void setStartsAt(OffsetDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public void setEndsAt(OffsetDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public void setReservedUntil(
        OffsetDateTime reservedUntil
    ) {
        this.reservedUntil = reservedUntil;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}