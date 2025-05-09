package com.gaser.docCollab.websocket;

import com.gaser.docCollab.server.Document.Document;
import java.util.UUID;
import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class WebSocketService {
  private ConcurrentHashMap<String, Document> documents = new ConcurrentHashMap<>(); // docId -> Document
  private ConcurrentHashMap<String, String> readCode = new ConcurrentHashMap<>(); // readcode - > docId
  private ConcurrentHashMap<String, String> writeCode = new ConcurrentHashMap<>(); // writecode - > docId
  private ConcurrentHashMap<String, Integer> lampertTime = new ConcurrentHashMap<>(); // docId - > lampertTime
  private ConcurrentHashMap<String, AtomicInteger> activeOperations = new ConcurrentHashMap<>(); // docId -> count of operations in progress
  private ConcurrentHashMap<String, AtomicInteger> activeJoins = new ConcurrentHashMap<>(); // docId -> count of active joins

  public HashMap<String, String> createDocument(int UID, String name, String initialContent) {
    // return id, either readonlycode or the read
    String id = UUID.randomUUID().toString();
    String readCode = UUID.randomUUID().toString();
    String editorCode = UUID.randomUUID().toString();

    Document document = new Document(id, name, UID);
    if (initialContent != null && !initialContent.isEmpty()) {
      document.initializeContent(initialContent);
    }

    document.setReadonlyCode(readCode);
    document.setEditorCode(editorCode);
    documents.put(id, document);

    this.readCode.put(readCode, id);
    this.writeCode.put(editorCode, id);
    this.lampertTime.put(id, 0);
    this.activeOperations.put(id, new AtomicInteger(0)); // Initialize operation counter

    HashMap<String, String> result = new HashMap<>();
    result.put("docID", id);
    result.put("readonlyCode", readCode);
    result.put("editorCode", editorCode);

    return result;
  }

  public HashMap<String, String> joinDocument(int UID, String code) {
    boolean isReadCode = readCode.containsKey(code);
    boolean isEditorCode = writeCode.containsKey(code);
    if (!isReadCode && !isEditorCode)
      return null;

    String docId = isReadCode ? readCode.get(code) : writeCode.get(code);
    
    Document document = documents.get(docId);
    document.addActiveUser(UID, 0);

    HashMap<String, String> result = new HashMap<>();
    result.put("readonlyCode", document.getReadonlyCode());
    result.put("docID", document.getId());
    result.put("crdt", document.getCrdt().serialize());
    result.put("isReader", readCode.containsKey(code) ? "true" : "false");

    // Document will be unlocked in the WebsocketController after broadcasting the join message
    
    return result;
  }

  public void leaveDocument(int UID, String docId) {
    documents.get(docId).removeActiveUser(UID);
  }

  public HashMap<Integer, Integer> getActiveUsers(String docId) {
    return documents.get(docId).getActiveUsers();
  }

  public List<Integer> getReconnectingUsers(String docId) {
    return documents.get(docId).getReconnectingUsers();
  }

  public void addReconnectingUser(String docId, int UID) {
    documents.get(docId).getReconnectingUsers().add(UID);
  }

  public boolean removeReconnectingUser(String docId, int UID) {
      List<Integer> reconnectingUsers = documents.get(docId).getReconnectingUsers();
      if (reconnectingUsers.contains(UID)) {
          reconnectingUsers.remove(Integer.valueOf(UID));
          return true; // Successfully removed
      }
      return false; // User was not in the list
  }

  public void handleOperations(String docId, List<Operation> operations) {
    documents.get(docId).getCrdt().handleOperations(operations);
  }

  public void handleCursorUpdate(String docId, Cursor cursor) {
    documents.get(docId).updateActiveUserCursor(cursor.getUID(), cursor.getPos());
  }

  public String getDocumentIDFromSessionCode(String sessionCode) {
    if (readCode.containsKey(sessionCode)) {
      return readCode.get(sessionCode);
    } else if (writeCode.containsKey(sessionCode)) {
      return writeCode.get(sessionCode);
    }
    return null;
  }

  public com.gaser.docCollab.client.CRDT getCRDT(String docId) {
    return documents.get(docId).getCrdt();
  }

  public String getReadOnlyCode(String docId) {
    return documents.get(docId).getReadonlyCode();
  }

  public String getEditorCode(String docId) {
    return documents.get(docId).getEditorCode();

  }

  public boolean isReadCode(String code) {
    return readCode.containsKey(code);
  }

  public Document getDocument(String docId) {
    return documents.get(docId);
  }

  public void setLampertTime(String docId, int time) {
    lampertTime.put(docId, time);
  }

  public int getLampertTime(String docId) {
    return lampertTime.get(docId);
  }

  // Operation lock methods
  public void incrementOperationCount(String docId) {
    activeOperations.computeIfAbsent(docId, k -> new AtomicInteger(0)).incrementAndGet();
  }

  public void decrementOperationCount(String docId) {
    AtomicInteger count = activeOperations.get(docId);
    if (count != null) {
      count.decrementAndGet();
    }
  }

  public boolean hasActiveOperations(String docId) {
    AtomicInteger count = activeOperations.get(docId);
    return count != null && count.get() > 0;
  }
  
  // Join lock methods
  public void incrementJoinCount(String docId) {
    activeJoins.computeIfAbsent(docId, k -> new AtomicInteger(0)).incrementAndGet();
  }

  public void decrementJoinCount(String docId) {
    AtomicInteger count = activeJoins.get(docId);
    if (count != null) {
      count.decrementAndGet();
    }
  }

  public boolean hasActiveJoins(String docId) {
    AtomicInteger count = activeJoins.get(docId);
    return count != null && count.get() > 0;
  }
}
