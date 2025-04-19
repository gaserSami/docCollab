package com.gaser.docCollab.server;

public class DocumentCreationRequest {
    private int UID;
    private String name;

    public DocumentCreationRequest() {
    }

    public DocumentCreationRequest(int UID, String name) {
        this.UID = UID;
        this.name = name;
    }

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
}