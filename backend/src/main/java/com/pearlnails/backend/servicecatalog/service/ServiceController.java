package com.pearlnails.backend.servicecatalog.service;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pearlnails.backend.servicecatalog.service.dto.CreateServiceRequest;
import com.pearlnails.backend.servicecatalog.service.dto.ServiceResponse;
import com.pearlnails.backend.servicecatalog.service.dto.UpdateServiceRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceController(
        ServiceCatalogService serviceCatalogService
    ) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public List<ServiceResponse> getServices(
        @RequestParam(required = false) Long categoryId
    ) {
        if (categoryId != null) {
            return serviceCatalogService
                .getServicesByCategory(categoryId);
        }

        return serviceCatalogService.getAllServices();
    }

    @GetMapping("/{id}")
    public ServiceResponse getServiceById(
        @PathVariable Long id
    ) {
        return serviceCatalogService.getServiceById(id);
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
        @Valid @RequestBody CreateServiceRequest request
    ) {
        ServiceResponse created =
            serviceCatalogService.createService(request);

        return ResponseEntity
            .created(
                URI.create("/api/services/" + created.id())
            )
            .body(created);
    }

    @PatchMapping("/{id}")
    public ServiceResponse updateService(
        @PathVariable Long id,
        @Valid @RequestBody UpdateServiceRequest request
    ) {
        return serviceCatalogService.updateService(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateService(
        @PathVariable Long id
    ) {
        serviceCatalogService.deactivateService(id);

        return ResponseEntity.noContent().build();
    }
}