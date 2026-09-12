package com.trelloclone.trello.board;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.trelloclone.trello.board.dto.BoardCreateRequest;
import com.trelloclone.trello.board.dto.GetBoardResponse;
import com.trelloclone.trello.membership.MembershipRepository;
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
                .orElseThrow(() -> new RuntimeException("Not a member"));

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Board board = new Board();
        board.setTitle(request.getTitle());
        board.setOrganization(organization);

        boardRepository.save(board);

        return new GetBoardResponse(board.getUuid(), board.getTitle(), organization.getUuid());
    }

    public GetBoardResponse getBoard(UUID organizationId, UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new RuntimeException("Board not found"));

        if (!board.getOrganization().getUuid().equals(organizationId)) {
            throw new RuntimeException("Board does not belong to organization");
        }

        membershipRepository.findByUserIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new RuntimeException("Not a member"));
        return new GetBoardResponse(board.getUuid(), board.getTitle(), organizationId);
    }
}
