package com.gaser.docCollab.server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// doubly linked list and a hashmap implementation of a CRDT
public class CRDT {
  doublylinkedHashMap rawDoc;
  Lock writeLock = new ReentrantReadWriteLock().writeLock();
  public CRDT(){
  }

  public void handleOperation(Operation operation) {
      if(operation.getOperationType() == OperationType.INSERT){
        insert(operation.getParentId(), operation.getValue(), operation.getUID() + "," + operation.getTime());
      } else if (operation.getOperationType() == OperationType.DELETE){
        delete(operation.getUID());
      }
  }

  private void insert(String parentId, char value, String id) {
    writeLock.lock();
    try{
      rawDoc.insert(parentId, value, id);
    } finally {
      writeLock.unlock();
    }
  }

  private void delete(String id) {
    writeLock.lock();
    try{
      rawDoc.markAsDeleted(id);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public String toString(){
    return rawDoc.toString();
  }

}
