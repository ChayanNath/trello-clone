package com.trelloclone.trello.organization;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public void createOrganization(OrganizationCreateRequest request) {

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());

        organizationRepository.save(organization);
    }

    @Transactional
    public void updateOrganization(UUID id, OrganizationUpdateRequest request) {

        Optional<Organization> organization = organizationRepository.findById(id);

        if (organization.isEmpty()) {
            throw new RuntimeException("Organization not found");
        }

        Organization org = organization.get();

        if (request.getName() != null) {
            org.setName(request.getName());
        }

        if (request.getDescription() != null) {
            org.setDescription(request.getDescription());
        }

    }

    public void deleteOrganization(UUID id) {
        Organization org = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        organizationRepository.delete(org);
    }

    public GetOrganizationResponse getOrganization(UUID id) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        return new GetOrganizationResponse(organization.getUuid(), organization.getName(),
                organization.getDescription());
    }

}
