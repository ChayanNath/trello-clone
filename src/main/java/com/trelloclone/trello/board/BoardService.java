package com.trelloclone.trello.board;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trelloclone.trello.board.dto.BoardCreateRequest;
import com.trelloclone.trello.board.dto.BoardUpdateRequest;
import com.trelloclone.trello.board.dto.GetBoardResponse;
import com.trelloclone.trello.common.exception.NotAuthorizedException;
import com.trelloclone.trello.common.exception.NotMemberException;
import com.trelloclone.trello.common.exception.ResourceNotFoundException;
import com.trelloclone.trello.membership.Membership;
import com.trelloclone.trello.membership.MembershipRepository;
import com.trelloclone.trello.membership.MembershipRole;
import com.trelloclone.trello.organization.Organization;
import com.trelloclone.trello.organization.OrganizationRepository;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final MembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;

    public BoardService(BoardRepository boardRepository, MembershipRepository membershipRepository,
            OrganizationRepository organizationRepository) {
        this.boardRepository = boardRepository;
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
    }

    public GetBoardResponse createBoard(UUID userId, UUID organizationId, BoardCreateRequest request) {
        membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new NotMemberException("Not a member"));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Board board = new Board();
        board.setTitle(request.getTitle());
        board.setOrganization(organization);

        boardRepository.save(board);

        return new GetBoardResponse(board.getUuid(), board.getTitle(), organization.getUuid());
    }

    public GetBoardResponse getBoard(UUID organizationId, UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getOrganization().getUuid().equals(organizationId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new NotMemberException("Not a member"));
        return new GetBoardResponse(board.getUuid(), board.getTitle(), organizationId);
    }

    public GetBoardResponse updateBoard(UUID organizationId, UUID boardId, UUID userId, BoardUpdateRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow((() -> new ResourceNotFoundException("Board not found")));

        if (!board.getOrganization().getUuid().equals(organizationId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        Membership membership = membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new NotMemberException("Not a member"));

        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new NotAuthorizedException("Not authorized");
        }

        board.setTitle(request.getTitle());

        boardRepository.save(board);

        return new GetBoardResponse(board.getUuid(), board.getTitle(), organizationId);
    }

    public void deleteBoard(UUID organizationId, UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow((() -> new ResourceNotFoundException("Board not found")));

        if (!board.getOrganization().getUuid().equals(organizationId)) {
            throw new ResourceNotFoundException("Board not found");
        }

        Membership membership = membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new NotMemberException("Not a member"));

        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new NotAuthorizedException("Not authorized");
        }

        boardRepository.delete(board);
    }
}
