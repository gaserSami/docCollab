package com.gaser.docCollab.websocket;
import java.util.HashMap;

import com.gaser.docCollab.client.CRDT;

public class Message {
  int UID;
  String content;
  String documentID;
  String documentTitle;
  private HashMap<Integer, Integer> activeUsers = new HashMap<>();
  private String crdt = null; // serialized CRDT object
  public HashMap<String, String> codes;
  public boolean isReader = false;

  public Message(int UID, String content) {
    this.UID = UID;
    this.content = content;
  }

  public Message() {
  }

  public int getUID() {
    return UID;
  }

  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  public String getDocumentID() {
    return documentID;
  }

  public void setUID(int UID) {
    this.UID = UID;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public HashMap<Integer, Integer> getActiveUsers() {
    return activeUsers;
  }

  public void setActiveUsers(HashMap<Integer, Integer> activeUsers) {
    this.activeUsers = activeUsers;
  }

  public String getCRDT() {
    return crdt;
  }

  public void setCRDT(String crdt) {
    this.crdt = crdt;
  }

  public String getDocumentTitle() {
    return documentTitle;
  }

  public void setDocumentTitle(String documentTitle) {
    this.documentTitle = documentTitle;
  }
  
}