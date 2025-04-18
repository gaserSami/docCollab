package com.gaser.docCollab.websocket;

public class Cursor {
  private int UID;
  private int pos;

  public Cursor(int UID, int pos) {
    this.UID = UID;
    this.pos = pos;
  }

  public Cursor() {
  }

  public int getUID() {
    return UID;
  }

  public void setUID(int UID) {
    this.UID = UID;
  }

  public int getPos() {
    return pos;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }

  @Override
  public String toString() {
    return "Cursor{" +
        "UID=" + UID +
        ", pos=" + pos +
        '}';
  }
}
