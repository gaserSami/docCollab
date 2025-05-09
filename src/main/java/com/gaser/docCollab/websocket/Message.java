package com.gaser.docCollab.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.stomp.StompHeaders;

import com.gaser.docCollab.client.CRDT;

public class Message {
  int UID;
  String content;
  String documentID;
  String documentTitle;
  private HashMap<Integer, Integer> activeUsers = new HashMap<Integer, Integer>();
  private List<Integer> reconnectingUsers;
  private String crdt = null; // serialized CRDT object
  public HashMap<String, String> codes;
  public boolean isReader = false;
  int lamportTime = 0;
  StompHeaders connectedHeaders = null;

  public Message(int UID, String content) {
    this.UID = UID;
    this.content = content;
    reconnectingUsers = new ArrayList<Integer>();
  }

  public Message() {
    reconnectingUsers = new ArrayList<Integer>();
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

  public void setLamportTime(int lamportTime) {
    this.lamportTime = lamportTime;
  }

  public int getLamportTime() {
    return lamportTime;
  }

  public List<Integer> getReconnectingUsers() {
    return reconnectingUsers;
  }

  public void setReconnectingUsers(List<Integer> reconnectingUsers) {
    this.reconnectingUsers = reconnectingUsers;
  }

  public void setHeaders(StompHeaders headers) {
    this.connectedHeaders = headers;
  }

  public StompHeaders getHeaders() {
    return connectedHeaders;
  }

}