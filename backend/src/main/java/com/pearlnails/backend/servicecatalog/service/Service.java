package com.pearlnails.backend.servicecatalog.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.pearlnails.backend.servicecatalog.category.ServiceCategory;

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
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategory category;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "buffer_minutes", nullable = false)
    private Integer bufferMinutes = 0;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

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

    protected Service() {
    }

    public Service(
        ServiceCategory category,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        Integer bufferMinutes,
        Integer displayOrder
    ) {
        this.category = category;
        this.name = name;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.bufferMinutes = bufferMinutes;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getBufferMinutes() {
        return bufferMinutes;
    }

    public void setBufferMinutes(Integer bufferMinutes) {
        this.bufferMinutes = bufferMinutes;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}