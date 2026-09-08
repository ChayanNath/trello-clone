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

        @PostMapping
        public ResponseEntity<ApiResponse<GetOrganizationResponse>> create(
                        @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OrganizationCreateRequest request) {
                GetOrganizationResponse organization = organizationService.createOrganization(request,
                                UUID.fromString(jwt.getSubject()));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiResponse<>(
                                                "success",
                                                "Organization created successfully",
                                                organization));
        }

        @PatchMapping("/{organizationId}")
        public ResponseEntity<ApiResponse<GetOrganizationResponse>> update(@AuthenticationPrincipal Jwt jwt,
                        @PathVariable UUID organizationId,
                        @Valid @RequestBody OrganizationUpdateRequest request) {

                UUID userId = UUID.fromString(jwt.getSubject());
                GetOrganizationResponse organization = organizationService.updateOrganization(organizationId, userId,
                                request);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(new ApiResponse<>(
                                                "success",
                                                "Organization updated successfully",
                                                organization));
        }

        @DeleteMapping("/{organizationId}")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID organizationId,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID userId = UUID.fromString(jwt.getSubject());
                organizationService.deleteOrganization(organizationId, userId);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(new ApiResponse<>(
                                                "success",
                                                "Organization deleted successfully",
                                                null));
        }

        @GetMapping("/{organizationId}")
        public ResponseEntity<ApiResponse<GetOrganizationResponse>> get(@PathVariable UUID organizationId,
                        @AuthenticationPrincipal Jwt jwt) {

                UUID userId = UUID.fromString(jwt.getSubject());
                GetOrganizationResponse organizationResponse = organizationService.getOrganization(organizationId,
                                userId);
                return ResponseEntity.status(HttpStatus.OK)
                                .body(new ApiResponse<>(
                                                "success",
                                                "Fetched organization",
                                                organizationResponse));
        }
}
