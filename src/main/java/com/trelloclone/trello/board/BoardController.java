package com.trelloclone.trello.board;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trelloclone.trello.board.dto.BoardCreateRequest;
import com.trelloclone.trello.board.dto.GetBoardResponse;
import com.trelloclone.trello.common.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/organization/{organizationId}/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GetBoardResponse>> createBoard(@PathVariable UUID organizationId,
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody BoardCreateRequest createRequest) {
        UUID userId = UUID.fromString(jwt.getSubject());

        GetBoardResponse board = boardService.createBoard(userId, organizationId, createRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("success", "Board created Successfully", board));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<GetBoardResponse>> getBoard(@PathVariable UUID organizationId,
            @PathVariable UUID boardId, @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        GetBoardResponse board = boardService.getBoard(organizationId, boardId, userId);

        return ResponseEntity.ok().body(new ApiResponse<>("success", "board fetched successfully", board));
    }
}
