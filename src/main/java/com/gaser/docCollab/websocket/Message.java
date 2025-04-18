package com.gaser.docCollab.websocket;

public class Message {
  int UID;
  String content;

  public Message(int UID, String content) {
    this.UID = UID;
    this.content = content;
  }

  public int getUID() {
    return UID;
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
}
