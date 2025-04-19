package com.gaser.docCollab.client;

import com.google.gson.Gson;

public class CharacterNode {
    private Character value;
    private int time;
    private int UID;
    private CharacterNode prev;
    private CharacterNode next;
    private boolean isDeleted;

    public CharacterNode() {
        this.prev = null;
        this.next = null;
        this.isDeleted = false;
    }

    public CharacterNode(Character value, int time, int UID) {
        this.value = value;
        this.time = time;
        this.UID = UID;
        this.prev = null;
        this.next = null;
        this.isDeleted = false;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public CharacterNode getPrev() {
        return prev;
    }

    public void setPrev(CharacterNode prev) {
        this.prev = prev;
    }

    public CharacterNode getNext() {
        return next;
    }

    public void setNext(CharacterNode next) {
        this.next = next;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getID() {
        return UID + "," + String.valueOf(time);
    }

    public String serialize() {
        return new Gson().toJson(this);
    }

    public static CharacterNode deserialize(String json) {
        return new Gson().fromJson(json, CharacterNode.class);
    }
}
