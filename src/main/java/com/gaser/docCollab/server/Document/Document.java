package com.gaser.docCollab.server.Document;

import java.util.HashMap;

import com.gaser.docCollab.client.CRDT;
import java.util.concurrent.ConcurrentHashMap;

public class Document {
    private String id;
    private String title;
    private int ownerId;
    private String readonlyCode;
    private String editorCode;
    private CRDT crdt;
    private HashMap<Integer, Integer> activeUsers = new HashMap<Integer, Integer>(); // user and its cursor pos

    public Document() {
        this.crdt = new CRDT();
    }

    public Document(String id, String title, int ownerId) {
        this.id = id;
        this.title = title;
        this.ownerId = ownerId;
        this.crdt = new CRDT();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getReadonlyCode() {
        return readonlyCode;
    }

    public void setReadonlyCode(String readonlyCode) {
        this.readonlyCode = readonlyCode;
    }

    public String getEditorCode() {
        return editorCode;
    }

    public void setEditorCode(String editorCode) {
        this.editorCode = editorCode;
    }

    public CRDT getCrdt() {
        return crdt;
    }

    public void setCrdt(CRDT crdt) {
        this.crdt = crdt;
    }

    public HashMap<Integer, Integer> getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(HashMap<Integer, Integer> activeUsers) {
        this.activeUsers = activeUsers;
    }

    public void addActiveUser(int UID, int cursorPos) {
        activeUsers.put(UID, cursorPos);
    }

    public void removeActiveUser(int UID) {
        activeUsers.remove(UID);
    }

    public void updateActiveUserCursor(int UID, int cursorPos) {
        activeUsers.put(UID, cursorPos);
    }

    public void clearActiveUsers() {
        activeUsers.clear();
    }

    public void initializeContent(String initialContent) {
        crdt.fromString(initialContent);
    }
}