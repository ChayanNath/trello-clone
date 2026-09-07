package com.trelloclone.trello.organization;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trelloclone.trello.membership.Membership;
import com.trelloclone.trello.membership.MembershipRepository;
import com.trelloclone.trello.membership.MembershipRole;
import com.trelloclone.trello.user.User;
import com.trelloclone.trello.user.UserRepository;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository,
            MembershipRepository membershipRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public GetOrganizationResponse createOrganization(OrganizationCreateRequest request, UUID userId) {

        Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setDescription(request.getDescription());

        organizationRepository.save(organization);

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Membership membership = new Membership();

        membership.setOrganization(organization);
        membership.setUser(user);
        membership.setRole(MembershipRole.ADMIN);

        membershipRepository.save(membership);

        return new GetOrganizationResponse(organization.getUuid(), organization.getName(),
                organization.getDescription());
    }

    @Transactional
    public GetOrganizationResponse updateOrganization(UUID id, OrganizationUpdateRequest request) {

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

        return new GetOrganizationResponse(org.getUuid(), org.getName(),
                org.getDescription());

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
