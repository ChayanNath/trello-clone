package com.trelloclone.trello.organization;

public class OrganizationUpdateRequest {

    private String name;

    private String description;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
