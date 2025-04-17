package com.gaser.docCollab.server;

import java.util.HashMap;

// o(1) time complexity for insertion and deletion
// o(1) for searching
public class doublylinkedHashMap {
  private CharacterNode head;
  private HashMap<String, CharacterNode> map;

  public doublylinkedHashMap() {
    this.head = null;
    this.map = new HashMap<>();
  }

  public void insert(String parentID, Character value, String ID){
    CharacterNode newNode = new CharacterNode(value, ID);
    insert(parentID, newNode);
  }

  public void insert(String parentID, CharacterNode newNode){
    if (head == null) {
      head = newNode;
      map.put(newNode.getID(), newNode);
      return;
    }

    CharacterNode parentNode = map.get(parentID);
    CharacterNode siblingNode = parentNode.getNext();
    if(siblingNode == null){
      parentNode.setNext(newNode);
      newNode.setPrev(parentNode);
      map.put(newNode.getID(), newNode);
      return;
    }

    String siblingID = siblingNode.getID();
    String siblingTime = siblingID.split(",")[1];
    String siblingUID = siblingID.split(",")[0];
    String newNodeID = newNode.getID();
    String newNodeTime = newNodeID.split(",")[1];
    String newNodeUID = newNodeID.split(",")[0];

    // the comparison should be on time then on uID
    // if the new node time is bigger then its newer then preceed to insert it normally and if they are equal check the uID
    // else the next node need to be added as a child to the sibling as the sibling is newer
    if(siblingTime.compareTo(newNodeTime) > 0 || siblingTime.compareTo(newNodeTime) == 0 && siblingUID.compareTo(newNodeUID) < 0){
      parentNode = siblingNode;
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

  public void markAsDeleted(String ID){
    map.get(ID).setDeleted(true);
  }

  public void markAsNotDeleted(String ID){
    map.get(ID).setDeleted(false);
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    CharacterNode current = head;
    while (current != null) {
      if (!current.isDeleted()) {
        sb.append(current.getValue());
      }
      current = current.getNext();
    }
    return sb.toString();
  }

}
