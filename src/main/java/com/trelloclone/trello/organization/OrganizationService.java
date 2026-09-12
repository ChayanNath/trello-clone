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
    public GetOrganizationResponse updateOrganization(UUID organizationId, UUID userId,
            OrganizationUpdateRequest request) {

        Optional<Organization> organization = organizationRepository.findById(organizationId);

        if (organization.isEmpty()) {
            throw new RuntimeException("Organization not found");
        }

        Membership membership = membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new RuntimeException("Not authorized");
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

    public void deleteOrganization(UUID organizationId, UUID userId) {

        Membership membership = membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("Not a member"));

        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new RuntimeException("Not authorized");
        }
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        organizationRepository.delete(org);
    }

    public GetOrganizationResponse getOrganization(UUID organizationId, UUID userId) {

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("Not a member"));

        return new GetOrganizationResponse(organization.getUuid(), organization.getName(),
                organization.getDescription());
    }

}
