package com.gaser.docCollab.server;

/**
 * Represents a character node in the CRDT structure.
 * Contains the character value, a unique IDentifier, and position information.
 */
public class CharacterNode {
    private Character value; // A character value
    private String ID; // uID,time
    private CharacterNode prev;
    private CharacterNode next;
    private boolean isDeleted; // Flag to indicate if the node is deleted

    public CharacterNode(Character value, String ID) {
        this.value = value;
        this.ID = ID;
        this.prev = null;
        this.next = null;
        this.isDeleted = false; // Default to not deleted
    }
    
    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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
}