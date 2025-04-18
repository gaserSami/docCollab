package com.gaser.docCollab.server;
import java.time.Instant;

public class Operation {
  private OperationType operationType;
  private int UID;
  private Instant time;
  private Character value;
  private String parentId;

  public Operation(OperationType operationType, int UID, Instant time, Character value, String parentId) {
    this.operationType = operationType;
    this.UID = UID;
    this.time = time;
    this.value = value;
    this.parentId = parentId;
  }

  public OperationType getOperationType() {
    return operationType;
  }

  public void setOperationType(OperationType operationType) {
    this.operationType = operationType;
  }

  public int getUID() {
    return UID;
  }

  public String getID() { // node id
    return UID +"," +time;
  }

  public void setUID(int UID) {
    this.UID = UID;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public Character getValue() {
    return value;
  }

  public void setValue(Character value) {
    this.value = value;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  @Override
  public String toString() {
    return "Operation{" +
            "operationType=" + operationType +
            ", UID='" + UID + '\'' +
            ", time='" + time + '\'' +
            ", value='" + value + '\'' +
            ", parentId='" + parentId + '\'' +
            '}';
  }
}
