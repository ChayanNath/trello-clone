package com.trelloclone.trello.organization;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trelloclone.trello.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody OrganizationCreateRequest request) {
        organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        "success",
                        "Organization created successfully",
                        null));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable UUID id,
            @Valid @RequestBody OrganizationUpdateRequest request) {

        organizationService.updateOrganization(id, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "success",
                        "Organization updated successfully",
                        null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {

        organizationService.deleteOrganization(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "success",
                        "Organization deleted successfully",
                        null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GetOrganizationResponse>> get(@PathVariable UUID id) {

        GetOrganizationResponse organizationResponse = organizationService.getOrganization(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(
                        "success",
                        "Fetched organization",
                        organizationResponse));
    }
}
