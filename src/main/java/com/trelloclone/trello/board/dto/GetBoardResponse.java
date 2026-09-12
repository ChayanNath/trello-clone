package com.trelloclone.trello.board.dto;

import java.util.UUID;

public class GetBoardResponse {

    private UUID id;
    private String title;
    private UUID organizationId;

    public GetBoardResponse(UUID id, String title, UUID organizationId) {
        this.id = id;
        this.title = title;
        this.organizationId = organizationId;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public UUID organizationId() {
        return organizationId;
    }
}
