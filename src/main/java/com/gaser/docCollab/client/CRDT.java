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
  private HashMap<String, List<CharacterNode>> pasteMap; // key and its children

  public CRDT() {
    this.head = new CharacterNode('#', Integer.MAX_VALUE, 0);
    this.map = new HashMap<>();
    this.waitingOn = new HashMap<>();
    pasteMap = new HashMap<String, List<CharacterNode>>();
    map.put(String.valueOf(0) + "," + Integer.MAX_VALUE, head);
  }

  public void handleOperations(List<Operation> operations) {
    try {
      writeLock.lock();
      if (operations.get(0).getOperationType() != OperationType.PASTE) {
        handleOperation(operations.get(0));
        return;
      }
      // if we are here we know that its a paste operation
      // we will have only one node which the parent
      if(operations.get(0).getSecondaryType() != SecondaryType.NORMAL)
      {
        List<CharacterNode> targetNodes = pasteMap.get(operations.get(0).getParentId());
        if (operations.get(0).getSecondaryType() == SecondaryType.UNDO) {
          for (int i = 0; i < targetNodes.size(); i++) {
            markAsDeleted(targetNodes.get(i).getID());
          }
          return;
        } else if (operations.get(0).getSecondaryType() == SecondaryType.REDO) {
          for (int i = 0; i < targetNodes.size(); i++) {
            markAsNotDeleted(targetNodes.get(i).getID());
          }
          return;
        }
      }

      // NORMAL PASTE
      // create a seperate list then simply call the insert on the root node
      CharacterNode localRoot = new CharacterNode(operations.get(0).getValue(), operations.get(0).getTime(),
          operations.get(0).getUID());
      map.put(localRoot.getID(), localRoot);
      pasteMap.put(localRoot.getID(), new ArrayList<>()); // all the children
      pasteMap.get(localRoot.getID()).add(localRoot);

      CharacterNode tail = localRoot;

      for (int i = 1; i < operations.size(); i++) {
        CharacterNode newNode = new CharacterNode(operations.get(i).getValue(), operations.get(i).getTime(),
            operations.get(i).getUID());
        // add to the list
        pasteMap.get(localRoot.getID()).add(newNode);
        tail.setNext(newNode);
        newNode.setPrev(tail);
        map.put(newNode.getID(), newNode);
        tail = newNode;
      }

      CharacterNode next = insert(operations.get(0).getParentId(), localRoot, true);
      tail.setNext(next);
      if(next != null) {
        next.setPrev(tail);
      }
    } catch (Exception e) {
      System.out.println("Error handling operations: " + e.getMessage());
      e.printStackTrace();
    } finally {
      writeLock.unlock();
    }
  }

  public void handleOperation(Operation operation) {
    if (operation.getSecondaryType() == SecondaryType.NORMAL) {
      if (operation.getOperationType() == OperationType.INSERT) {
        insert(operation.getParentId(), operation.getValue(), operation.getUID(), operation.getTime());
      } else {
        // remove
        markAsDeleted(operation.getParentId()); // here its the node id not the parent id
        // same fl ba2e
      }
    } else if (operation.getSecondaryType() == SecondaryType.UNDO) {
      if (operation.getOperationType() == OperationType.INSERT) {
        markAsDeleted(operation.getParentId());
      } else if (operation.getOperationType() == OperationType.DELETE) {
        markAsNotDeleted(operation.getParentId());
      }
    } else { // redo
      if (operation.getOperationType() == OperationType.INSERT) {
        markAsNotDeleted(operation.getParentId());
      } else if (operation.getOperationType() == OperationType.DELETE) {
        markAsDeleted(operation.getParentId());
      }
    }
  }

  public void insert(String parentID, Character value, int UID, int time) {
    try {
      writeLock.lock();
      if (!map.containsKey(parentID)) {
        waitingOn.putIfAbsent(parentID, new ArrayList<>());
        waitingOn.get(parentID).add(new CharacterNode(value, time, UID));
        return;
      }
      CharacterNode newNode = new CharacterNode(value, time, UID);
      insert(parentID, newNode);
      // inserted so we check
      if (waitingOn.containsKey(newNode.getID())) {
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

  private CharacterNode insert(String parentID, CharacterNode newNode, boolean isPaste) {
    CharacterNode parentNode = map.get(parentID);
    CharacterNode siblingNode = parentNode.getNext();

    while (true) {
      if (siblingNode == null)
        break;

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
      return null;
    }

    // normal insertion
    newNode.setPrev(parentNode);
    if(!isPaste) newNode.setNext(siblingNode);
    parentNode.setNext(newNode);
    if (!isPaste && newNode.getNext() != null) {
      newNode.getNext().setPrev(newNode);
    }
    map.put(newNode.getID(), newNode);

    return siblingNode;
  }

  private CharacterNode insert(String parentID, CharacterNode newNode) {
    return insert(parentID, newNode, false);
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

  public void fromString(String content) {
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
    Type listType = new TypeToken<List<NodeDTO>>() {
    }.getType();
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

  public void setPasteMapFromString(String pasteMapStr) {
    this.pasteMap = new HashMap<>();
    if (pasteMapStr == null || pasteMapStr.isEmpty()) {
      return;
    }
    
    String[] entries = pasteMapStr.split("\\|");
    for (String entry : entries) {
      String[] parts = entry.split(":");
      if (parts.length != 2) continue;
      
      String key = parts[0];
      String[] nodeStrings = parts[1].split(";");
      List<CharacterNode> nodes = new ArrayList<>();
      
      for (String nodeStr : nodeStrings) {
        String[] nodeData = nodeStr.split(",");
        if (nodeData.length != 3) continue;
        
        char value = nodeData[0].charAt(0);
        int time = Integer.parseInt(nodeData[1]);
        int uid = Integer.parseInt(nodeData[2]);
        
        CharacterNode node = new CharacterNode(value, time, uid);
        nodes.add(node);
      }
      
      if (!nodes.isEmpty()) {
        this.pasteMap.put(key, nodes);
      }
    }
  }
  
  public String getPasteMapAsString() {
    if (pasteMap == null || pasteMap.isEmpty()) {
      return "";
    }
    
    StringBuilder result = new StringBuilder();
    boolean firstEntry = true;
    
    for (String key : pasteMap.keySet()) {
      List<CharacterNode> nodes = pasteMap.get(key);
      if (nodes == null || nodes.isEmpty()) continue;
      
      if (!firstEntry) {
        result.append("|");
      } else {
        firstEntry = false;
      }
      
      result.append(key).append(":");
      
      boolean firstNode = true;
      for (CharacterNode node : nodes) {
        if (!firstNode) {
          result.append(";");
        } else {
          firstNode = false;
        }
        
        result.append(node.getValue())
              .append(",")
              .append(node.getTime())
              .append(",")
              .append(node.getUID());
      }
    }
    
    return result.toString();
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
