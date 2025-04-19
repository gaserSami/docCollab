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
        return String.format(
                "{\n" +
                        "  \"value\": \"%s\",\n" +
                        "  \"time\": %d,\n" +
                        "  \"UID\": %d,\n" +
                        "  \"prev\": %s,\n" +
                        "  \"next\": %s,\n" +
                        "  \"isDeleted\": %b\n" +
                        "}",
                value,
                time,
                UID,
                prev != null ? prev.getID() : null,
                next != null ? next.getID() : null,
                isDeleted
        );
    }

    public static CharacterNode deserialize(String json) {
        CharacterNode node = new CharacterNode();
        
        String cleaned = json.trim().substring(1, json.length() - 1).trim();
        
        int valueStart = cleaned.indexOf("\"value\": \"") + 10;
        int valueEnd = cleaned.indexOf("\"", valueStart);
        String valueStr = cleaned.substring(valueStart, valueEnd);
        node.setValue(valueStr.isEmpty() ? null : valueStr.charAt(0));
        
        int timeStart = cleaned.indexOf("\"time\": ") + 8;
        int timeEnd = cleaned.indexOf(",", timeStart);
        node.setTime(Integer.parseInt(cleaned.substring(timeStart, timeEnd).trim()));
        
        int uidStart = cleaned.indexOf("\"UID\": ") + 7;
        int uidEnd = cleaned.indexOf(",", uidStart);
        node.setUID(Integer.parseInt(cleaned.substring(uidStart, uidEnd).trim()));
        
        // Extract prev
        int prevStart = cleaned.indexOf("\"prev\": ") + 8;
        int prevEnd = cleaned.indexOf(",", prevStart);
        String prevStr = cleaned.substring(prevStart, prevEnd).trim();
        
        int nextStart = cleaned.indexOf("\"next\": ") + 8;
        int nextEnd = cleaned.indexOf(",", nextStart);
        String nextStr = cleaned.substring(nextStart, nextEnd).trim();
        
        // Extract isDeleted
        int isDeletedStart = cleaned.indexOf("\"isDeleted\": ") + 13;
        int isDeletedEnd = cleaned.length();
        node.setDeleted(Boolean.parseBoolean(cleaned.substring(isDeletedStart, isDeletedEnd).trim()));
        
        return node;
    }
}
