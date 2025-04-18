package com.gaser.docCollab.client;

import java.time.Instant;

public class CharacterNode {
  private Character value;
    private Instant time;
    private int UID;
    private CharacterNode prev;
    private CharacterNode next;
    private boolean isDeleted;

    public CharacterNode(Character value, Instant time, int UID) {
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

   public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
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
        return UID + "," + time.toString();
    }
}
