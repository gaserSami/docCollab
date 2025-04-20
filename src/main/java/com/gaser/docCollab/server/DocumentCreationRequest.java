package com.gaser.docCollab.server;

public class DocumentCreationRequest {
    private int UID;
    private String name;
    private String content;

    // Constructors
    public DocumentCreationRequest() {
    }

    public DocumentCreationRequest(int UID, String name, String content) {
        this.UID = UID;
        this.name = name;
        this.content = content;
    }

    // Getters and setters
    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}