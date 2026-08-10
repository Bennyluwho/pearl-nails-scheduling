package com.pearlnails.backend.servicecatalog.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.pearlnails.backend.servicecatalog.category.ServiceCategory;
import com.pearlnails.backend.servicecatalog.category.ServiceCategoryRepository;
import com.pearlnails.backend.servicecatalog.service.dto.CreateServiceRequest;
import com.pearlnails.backend.servicecatalog.service.dto.ServiceResponse;
import com.pearlnails.backend.servicecatalog.service.dto.UpdateServiceRequest;

@org.springframework.stereotype.Service
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;

    public ServiceCatalogService(
        ServiceRepository serviceRepository,
        ServiceCategoryRepository categoryRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices() {
        return serviceRepository
            .findAllByOrderByCategoryDisplayOrderAscDisplayOrderAscNameAsc()
            .stream()
            .map(ServiceResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        return ServiceResponse.from(findServiceById(id));
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByCategory(Long categoryId) {
        return serviceRepository
            .findByCategoryIdOrderByDisplayOrderAscNameAsc(categoryId)
            .stream()
            .map(ServiceResponse::from)
            .toList();
    }

    @Transactional
    public ServiceResponse createService(
        CreateServiceRequest request
    ) {
        ServiceCategory category = findActiveCategory(
            request.categoryId()
        );

        String normalizedName = request.name().trim();

        if (
            serviceRepository.existsByCategoryIdAndNameIgnoreCase(
                category.getId(),
                normalizedName
            )
        ) {
            throw new IllegalArgumentException(
                "A service with that name already exists in this category"
            );
        }

        int bufferMinutes =
            request.bufferMinutes() != null
                ? request.bufferMinutes()
                : 0;

        int displayOrder =
            request.displayOrder() != null
                ? request.displayOrder()
                : 0;

        Service service = new Service(
            category,
            normalizedName,
            request.description(),
            request.price(),
            request.durationMinutes(),
            bufferMinutes,
            displayOrder
        );

        Service saved = serviceRepository.save(service);

        return ServiceResponse.from(saved);
    }

    @Transactional
    public ServiceResponse updateService(
        Long id,
        UpdateServiceRequest request
    ) {
        Service service = findServiceById(id);

        ServiceCategory targetCategory = service.getCategory();

        if (request.categoryId() != null) {
            targetCategory = findActiveCategory(
                request.categoryId()
            );
        }

        String targetName = service.getName();

        if (request.name() != null) {
            targetName = request.name().trim();
        }

        if (
            request.categoryId() != null ||
            request.name() != null
        ) {
            serviceRepository
                .findByCategoryIdAndNameIgnoreCase(
                    targetCategory.getId(),
                    targetName
                )
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                        "A service with that name already exists in this category"
                    );
                });
        }

        if (request.categoryId() != null) {
            service.setCategory(targetCategory);
        }

        if (request.name() != null) {
            service.setName(targetName);
        }

        if (request.description() != null) {
            service.setDescription(request.description());
        }

        if (request.price() != null) {
            service.setPrice(request.price());
        }

        if (request.durationMinutes() != null) {
            service.setDurationMinutes(
                request.durationMinutes()
            );
        }

        if (request.bufferMinutes() != null) {
            service.setBufferMinutes(
                request.bufferMinutes()
            );
        }

        if (request.displayOrder() != null) {
            service.setDisplayOrder(
                request.displayOrder()
            );
        }

        if (request.active() != null) {
            service.setActive(request.active());
        }

        return ServiceResponse.from(service);
    }

    @Transactional
    public void deactivateService(Long id) {
        Service service = findServiceById(id);
        service.setActive(false);
    }

    private Service findServiceById(Long id) {
        return serviceRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Service not found: " + id
                )
            );
    }

    private ServiceCategory findActiveCategory(Long id) {
        ServiceCategory category = categoryRepository
            .findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Service category not found: " + id
                )
            );

        if (!category.getActive()) {
            throw new IllegalArgumentException(
                "Cannot use an inactive service category"
            );
        }

        return category;
    }
}
