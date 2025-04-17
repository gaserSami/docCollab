package com.gaser.docCollab.server;

public class Operation {
  private OperationType operationType;
  private String UID;
  private String time;
  private Character value;
  private String parentId;

  public Operation(OperationType operationType, String UID, String time, Character value, String parentId) {
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

  public String getUID() {
    return UID;
  }

  public void setUID(String UID) {
    this.UID = UID;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
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
