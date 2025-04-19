package com.gaser.docCollab.client;

import java.util.HashMap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;
import com.google.gson.Gson;

public class CRDT {
  private CharacterNode head;
  private HashMap<String, CharacterNode> map;
  private transient Lock writeLock = new ReentrantReadWriteLock().writeLock();

  public CRDT() {
    this.head = new CharacterNode('#', Integer.MAX_VALUE, 0);
    this.map = new HashMap<>();
    map.put(String.valueOf(0) + "," + Integer.MAX_VALUE, head);
  }

  public void handleOperation(Operation operation) {
    if (operation.getOperationType() == OperationType.INSERT) {
      insert(operation.getParentId(), operation.getValue(), operation.getUID(), operation.getTime());
    } else {
      System.out
          .println("Node with ID: " + operation.getParentId() + " exists in map: "
              + map.containsKey(operation.getParentId()));
      markAsDeleted(operation.getParentId());
    }
    // TODO handle redo and undo
  }

  public void insert(String parentID, Character value, int UID, int time) {
    try {
      writeLock.lock();
      CharacterNode newNode = new CharacterNode(value, time, UID);
      insert(parentID, newNode);
    } finally {
      writeLock.unlock();
    }
  }

  private void insert(String parentID, CharacterNode newNode) {
    CharacterNode parentNode = map.get(parentID);
    CharacterNode siblingNode = parentNode.getNext();

    if (siblingNode == null) {
      parentNode.setNext(newNode);
      newNode.setPrev(parentNode);
      System.out.println("Inserting node with ID: " + newNode.getID() + " after node with ID: " + parentNode.getID());
      map.put(newNode.getID(), newNode);
      return;
    }

    int siblingTime = siblingNode.getTime();
    int siblingUID = siblingNode.getUID();
    int newNodeTime = newNode.getTime();
    int newNodeUID = newNode.getUID();

    // Compare time
    // If times are equal, compare uids
    int timeComparison = Integer.compare(siblingTime, newNodeTime);
    if (timeComparison > 0 || (timeComparison == 0 && siblingUID < newNodeUID)) {
      insert(siblingNode.getID(), newNode);
      return;
    }

    // normal insertion
    newNode.setPrev(parentNode);
    newNode.setNext(parentNode.getNext());
    parentNode.setNext(newNode);
    if (newNode.getNext() != null) {
      newNode.getNext().setPrev(newNode);
    }
    map.put(newNode.getID(), newNode);
  }

  public void markAsDeleted(String id) {
    try {
      writeLock.lock();
      map.get(id).setDeleted(true);
    } finally {
      writeLock.unlock();
    }
  }

  public CharacterNode getNodeFromPosition(int position) {
    CharacterNode current = null;
    try {
      writeLock.lock();
      current = head;
      int index = 0;
      while (current != null) {
        System.out.println("Current Idx: " + index + " Current Node: " + current.getValue());
        if (!current.isDeleted()) {
          if (index == position) {
            return current;
          }
          index++;
        }
        current = current.getNext();
      }
    } finally {
      writeLock.unlock();
    }
    return current;
  }

  public void markAsNotDeleted(String id) {
    try {
      writeLock.lock();
      map.get(id).setDeleted(false);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    CharacterNode current = head.getNext(); // skip the dummy head node
    while (current != null) {
      if (!current.isDeleted()) {
        sb.append(current.getValue());
      }
      current = current.getNext();
    }
    return sb.toString();
  }

  public String serialize() {
    try{
      writeLock.lock();
      return new Gson().toJson(this);
    } finally{
      writeLock.unlock();
    }
  }

  public static CRDT deserialize(String json) {
    return new Gson().fromJson(json, CRDT.class);
  }
}
