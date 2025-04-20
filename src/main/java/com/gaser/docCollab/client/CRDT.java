package com.gaser.docCollab.client;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;
import com.gaser.docCollab.server.SecondaryType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CRDT {
  private CharacterNode head;
  private HashMap<String, CharacterNode> map;
  private transient Lock writeLock = new ReentrantReadWriteLock().writeLock();
  private HashMap<String, List<CharacterNode>> waitingOn;

  public CRDT() {
    this.head = new CharacterNode('#', Integer.MAX_VALUE, 0);
    this.map = new HashMap<>();
    this.waitingOn = new HashMap<>();
    map.put(String.valueOf(0) + "," + Integer.MAX_VALUE, head);
  }

  public void handleOperation(Operation operation) {
    if(operation.getSecondaryType() == SecondaryType.NORMAL){
      if (operation.getOperationType() == OperationType.INSERT) {
        insert(operation.getParentId(), operation.getValue(), operation.getUID(), operation.getTime());
      } else {
        // remove
        markAsDeleted(operation.getParentId());
      }
    } else if(operation.getSecondaryType() == SecondaryType.UNDO){
      if(operation.getOperationType() == OperationType.INSERT){
        markAsDeleted(map.get(operation.getParentId()).getNext().getID());
      } else if(operation.getOperationType() == OperationType.DELETE){
        markAsNotDeleted(operation.getParentId());
      }
    } else{ // redo
      if(operation.getOperationType() == OperationType.INSERT){
        markAsNotDeleted(map.get(operation.getParentId()).getNext().getID());
      } else if(operation.getOperationType() == OperationType.DELETE){
        markAsDeleted(operation.getParentId());
      }
    }
  }

  public void insert(String parentID, Character value, int UID, int time) {
    try {
      writeLock.lock();
      if(!map.containsKey(parentID)) {
        waitingOn.putIfAbsent(parentID, new ArrayList<>());
        waitingOn.get(parentID).add(new CharacterNode(value, time, UID));
        return;
      }
      CharacterNode newNode = new CharacterNode(value, time, UID);
      insert(parentID, newNode);
      // inserted so we check
      if(waitingOn.containsKey(newNode.getID())){
        for (CharacterNode node : waitingOn.get(newNode.getID())) {
          insert(newNode.getID(), node.getValue(), node.getUID(), node.getTime());
        }
        waitingOn.remove(newNode.getID());
      }
    } catch (Exception e) {
      System.out.println("Error inserting node: " + e.getMessage());
      e.printStackTrace();
    } finally {
      writeLock.unlock();
    }
  }

  private void insert(String parentID, CharacterNode newNode) {
    CharacterNode parentNode = map.get(parentID);
    CharacterNode siblingNode = parentNode.getNext();
    
    while(true){
      if(siblingNode == null) break;
      
      int siblingTime = siblingNode.getTime();
      int siblingUID = siblingNode.getUID();
      int newNodeTime = newNode.getTime();
      int newNodeUID = newNode.getUID();
      int timeComparison = Integer.compare(siblingTime, newNodeTime);

      if (timeComparison < 0 || (timeComparison == 0 && siblingUID >= newNodeUID)) {
        break;
      }
    
      parentNode = siblingNode;
      siblingNode = parentNode.getNext();
    }

    if (siblingNode == null) {
      parentNode.setNext(newNode);
      newNode.setPrev(parentNode);
      map.put(newNode.getID(), newNode);
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

  public void fromString(String content){
    CharacterNode current = head;

    for (int i = 0; i < content.length(); i++) {
      // CharacterNode newNode = new CharacterNode(content.charAt(i), i, 0);
      CharacterNode newNode = new CharacterNode(content.charAt(i), -1, Integer.MAX_VALUE - i);
      current.setNext(newNode);
      newNode.setPrev(current);
      current = newNode;
      map.put(newNode.getID(), newNode);
    }
  }

  public String serialize() {
        List<NodeDTO> dtoList = new ArrayList<>();
        for (CharacterNode node : map.values()) {
            NodeDTO dto = new NodeDTO();
            dto.value = node.getValue();
            dto.time = node.getTime();
            dto.UID = node.getUID();
            dto.prevId = node.getPrev() != null ? node.getPrev().getID() : null;
            dto.nextId = node.getNext() != null ? node.getNext().getID() : null;
            dto.isDeleted = node.isDeleted();
            dtoList.add(dto);
        }
        return new Gson().toJson(dtoList);
  }

public static CRDT deserialize(String json) {
        Type listType = new TypeToken<List<NodeDTO>>() {}.getType();
        List<NodeDTO> dtoList = new Gson().fromJson(json, listType);

        CRDT crdt = new CRDT();
        crdt.map.clear();

        // First pass: create all nodes
        HashMap<String, CharacterNode> tempMap = new HashMap<>();
        for (NodeDTO dto : dtoList) {
            CharacterNode node = new CharacterNode(dto.value, dto.time, dto.UID);
            node.setDeleted(dto.isDeleted);
            tempMap.put(node.getID(), node);
        }

        // Second pass: set prev/next links
        for (NodeDTO dto : dtoList) {
            CharacterNode node = tempMap.get(dto.UID + "," + dto.time);
            if (dto.prevId != null) {
                node.setPrev(tempMap.get(dto.prevId));
            }
            if (dto.nextId != null) {
                node.setNext(tempMap.get(dto.nextId));
            }
        }

        // Update CRDT head and map
        // Identify head (dummy with UID=0 and time=MAX_VALUE)
        String headId = 0 + "," + Integer.MAX_VALUE;
        crdt.head = tempMap.get(headId);
        crdt.map.putAll(tempMap);
        return crdt;
    }

    private static class NodeDTO {
      Character value;
      int time;
      int UID;
      String prevId;
      String nextId;
      boolean isDeleted;
  }

}
