package com.gaser.docCollab.client;
import java.time.Instant;
import java.util.HashMap;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;

public class CRDT {
  private CharacterNode head;
  private HashMap<String, CharacterNode> map;
  Lock writeLock = new ReentrantReadWriteLock().writeLock();

  public CRDT() {
    this.head = new CharacterNode('#', Instant.MAX, 0);
    this.map = new HashMap<>();
    map.put(String.valueOf(0)+Instant.MAX, head);
  }

  public void handleOperation(Operation operation){
    if(operation.getOperationType() == OperationType.INSERT){
      insert(operation.getParentId(), operation.getValue(), operation.getUID(), Instant.now());
    } else{
      markAsDeleted(operation.getID());
    }
  }

  public void insert(String parentID, Character value, int UID, Instant time){
    try{
      writeLock.lock();
      CharacterNode newNode = new CharacterNode(value, time, UID);
      insert(parentID, newNode);
    } finally{
      writeLock.unlock();
    }
  }

  private void insert(String parentID, CharacterNode newNode){
    CharacterNode parentNode = map.get(parentID);
    CharacterNode siblingNode = parentNode.getNext();
    
    if(siblingNode == null){
      parentNode.setNext(newNode);
      newNode.setPrev(parentNode);
      map.put(newNode.getID(), newNode);
      return;
    }

    Instant siblingTime = siblingNode.getTime();
    int siblingUID = siblingNode.getUID();
    Instant newNodeTime = siblingNode.getTime();
    int newNodeUID = siblingNode.getUID();

    // Compare time
    // If times are equal, compare uids
    int timeComparison = siblingTime.compareTo(newNodeTime);
    if (timeComparison > 0 || (timeComparison == 0 && siblingUID < newNodeUID)) {
      insert(siblingNode.getID(), newNode);
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

  public void markAsDeleted(String id){
    try{
      writeLock.lock();
      map.get(id).setDeleted(true);
    }
    finally{
      writeLock.unlock();
    }
  }

  public CharacterNode getNodeFromPosition(int position){
    try{
      writeLock.lock();
      CharacterNode current = head.getNext(); // skip the dummy head node
      int index = 0;
      while (current != null) {
        if (!current.isDeleted()) {
          if (index == position) {
            return current;
          }
          index++;
        }
        current = current.getNext();
      }
    } finally{
      writeLock.unlock();
    }
    return null;
  }

  public void markAsNotDeleted(String id){
    try{
      writeLock.lock();
      map.get(id).setDeleted(false);
    }
    finally{
      writeLock.unlock();
    }
  }

  @Override
  public String toString(){
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
}
