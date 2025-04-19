package com.gaser.docCollab.websocket;
import com.gaser.docCollab.server.Document.Document;
import java.util.UUID;
import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;

import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class WebSocketService {
  private HashMap<String, Document> documents = new HashMap<>(); // docId -> Document
  private HashMap<String, String> readCode = new HashMap<>(); // readcode - > docId
  private HashMap<String, String> writeCode = new HashMap<>(); // writecode - > docId

  public HashMap<String, String> createDocument(int UID, String name, String initialContent){
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

    HashMap<String, String> result = new HashMap<>();
    result.put("docID", id);
    result.put("readonlyCode", readCode);
    result.put("editorCode", editorCode);

    return result;
  }
  
  public HashMap<String, String> joinDocument(int UID, String code){
    boolean isReadCode = readCode.containsKey(code);
    boolean isEditorCode = writeCode.containsKey(code);
    if (!isReadCode && !isEditorCode) return null;

    String docId = isReadCode ? readCode.get(code) : writeCode.get(code);
    Document document = documents.get(docId);
    document.addActiveUser(UID, 0);

    HashMap<String, String> result = new HashMap<>();
    result.put("readonlyCode", document.getReadonlyCode());
    result.put("docID", document.getId());
    result.put("crdt", document.getCrdt().serialize());
    result.put("isReader", readCode.containsKey(code) ? "true" : "false");

    return result;
  }

  public void leaveDocument(int UID, String docId){
    documents.get(docId).removeActiveUser(UID);
  }

  public HashMap<Integer, Integer> getActiveUsers(String docId) {
    return documents.get(docId).getActiveUsers();
  }


  public void handleOperation(String docId, Operation operation) {
    documents.get(docId).getCrdt().handleOperation(operation);
    System.out.println("server handled operation on document: " + docId + " operation: " + operation.toString());
    System.out.println("server crdt current state: " + documents.get(docId).getCrdt().toString());
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

}


