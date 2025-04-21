package com.gaser.docCollab.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaser.docCollab.UI.CollaborativeUI;
import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;
import com.gaser.docCollab.server.SecondaryType;
import com.gaser.docCollab.websocket.Cursor;
import com.gaser.docCollab.websocket.Message;

import org.springframework.web.socket.sockjs.client.Transport;
import java.util.List;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyStompClient {
  private StompSession session;
  private boolean reconnecting = false;
  private int UID;
  private String documentID = null;
  private String documentTitle = "null";
  public HashMap<String, String> codes;
  private HashMap<Integer, Integer> activeUsers;
  private boolean isReader = false;
  private CRDT crdt;
  private CollaborativeUI ui;
  private int lamportTime = 0;
  private Stack<Operation> undoStack = new Stack<>();
  private Stack<Operation> redoStack = new Stack<>();
  private String sessionCode = "";
  List<List<Operation>> bufferedOperations = new ArrayList<>();

  public MyStompClient(int UID, CollaborativeUI ui) {
    this.UID = UID;
    this.ui = ui;
    this.crdt = new CRDT();
    this.activeUsers = new HashMap<Integer, Integer>();
    this.codes = new HashMap<String, String>();
  }

  public int getLamportTime() {
    return lamportTime;
  }

  public void setLamportTime(int lamportTime) {
    this.lamportTime = lamportTime;
  }

  public void incrementLamportTime() {
    lamportTime++;
  }

  // public void onUserJoin(int UID) {
  // activeUserIds.add(UID);
  // cursorPositions.add(0);
  // }

  // public void onUserLeave(int UID) {
  // for (int i = 0; i < activeUserIds.size(); i++) {
  // if (activeUserIds.get(i).equals(UID)) {
  // activeUserIds.remove(i);
  // cursorPositions.remove(i);
  // break;
  // }
  // }
  // }

  public void setActiveUsers(HashMap<Integer, Integer> activeUsers) {
    this.activeUsers = activeUsers;
  }

  public void onSocketOperations(List<Operation> operations) {
    onSocketOperations(operations, false);
  }

  public void onSocketOperations(List<Operation> operations, boolean force) {
    if (operations.get(0).getUID() == getUID() && !force)
      return;
    lamportTime = Math.max(lamportTime, operations.get(operations.size() - 1).getTime()) + 1;
    getCrdt().handleOperations(operations);
    String crdtString = getCrdt().toString();
    javax.swing.SwingUtilities.invokeLater(() -> {
      getUI().getMainPanel().updateDocumentContent(crdtString, force);
    });
  }

  public void onSocketUsers(Message message) {
    setActiveUsers(message.getActiveUsers());
    lamportTime = Math.max(lamportTime, message.getLamportTime());

    getUI().getSidebarPanel().updateActiveUsers(
        IntStream.range(0, getActiveUserIds().size())
            .mapToObj(idx -> {
              Integer id = getActiveUserIds().get(idx);
              Integer position = getCursorPositions().get(idx);
              String userLabel = id.equals(getUID()) ? "User " + id + " (you)" : "User " + id;
              return userLabel + " • Position: " + position;
            })
            .collect(java.util.stream.Collectors.toList()));
  }

  public void onSocketCursors(Cursor cursor, boolean force) {
    if (cursor.getUID() == getUID() && !force)
      return;
    activeUsers.put(cursor.getUID(), cursor.getPos());

    getUI().getSidebarPanel().updateActiveUsers(
        IntStream.range(0, getActiveUserIds().size())
            .mapToObj(idx -> {
              Integer id = getActiveUserIds().get(idx);
              Integer position = getCursorPositions().get(idx);
              String userLabel = id.equals(getUID()) ? "User " + id + " (you)" : "User " + id;
              return userLabel + " • Position: " + position;
            })
            .collect(java.util.stream.Collectors.toList()));
  }

  public void onSocketCursors(Cursor cursor) {
    onSocketCursors(cursor, false);
  }

  private void listen() {
    String tmp = "/topic/operations/" + getDocumentID();

    session.subscribe("/topic/operations/" + getDocumentID(), new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return List.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        try {
          if (payload instanceof List) {
            List<?> rawList = (List<?>) payload;
            List<Operation> operations = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            for (Object item : rawList) {
              // Convert each LinkedHashMap to Operation
              Operation op = mapper.convertValue(item, Operation.class);
              operations.add(op);
            }

            onSocketOperations(operations);
          } else {
            System.out.println("Received unexpected payload type: " + payload.getClass());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    /////////////////
    session.subscribe("/topic/users/" + getDocumentID(), new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return Message.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        try {
          if (payload instanceof Message) {
            Message message = (Message) payload;
            onSocketUsers(message);
          } else {
            System.out.println("Received unexpected payload type: " + payload.getClass());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    session.subscribe("/topic/cursors/" + getDocumentID(), new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return Cursor.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        try {
          if (payload instanceof Cursor) {
            Cursor cursor = (Cursor) payload;
            onSocketCursors(cursor);
          } else {
            System.out.println("Received unexpected payload type: " + payload.getClass());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public boolean connectToWebSocket() {
    try {
      List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
      SockJsClient sockJsClient = new SockJsClient(transports);
      WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

      stompClient.setMessageConverter(new MappingJackson2MessageConverter());

      StompSessionHandler sessionHandler = new MyStompSessionHandler(this);
      String url = "ws://localhost:8080/ws";

      session = stompClient.connectAsync(url, sessionHandler).get();
      return true;
    } catch (Exception e) {
      System.out.println("Error connecting to WebSocket: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  public void disconnectFromWebSocket() {
    if (session != null && session.isConnected()) {
      try {
        String tmp = this.documentID;
        // First leave the current document to notify other users
        if (this.documentID != null) {
          leaveDocument(this.documentID);
          this.documentID = null;
          this.codes = new HashMap<>();
          crdt = new CRDT();
          documentTitle = "null";
          isReader = false;
          undoStack.clear();
          redoStack.clear();
          sessionCode = "";
        }

        // Then disconnect the session
        session.disconnect();

        // Clear session reference
        session = null;

        // Clear active users and cursor positions
        activeUsers.clear();
      } catch (Exception e) {
        System.out.println("Error disconnecting from WebSocket: " + e.getMessage());
        e.printStackTrace();
      }
    }
  }

  public void handleUnexpectedDisconnect() {
    // The session is already disconnected at this point
    System.out.println("Handling unexpected disconnection");
    
    // Update UI to show disconnection status
    // javax.swing.SwingUtilities.invokeLater(() -> {
    //     ui.showErrorMessage("Connection to server lost. Please reconnect.");
    // });
    
    attemptReconnection();
  }

  private void attemptReconnection() {
    if (reconnecting) {
        System.out.println("Already attempting to reconnect. Please wait.");
        return;
    }
    reconnecting = true;
    ui.getTopBarPanel().markDisconnected();
    new Thread(() -> {
        int attempts = 0;
        final int maxAttempts = 10;
        boolean reconnected = false;
        
        while (attempts < maxAttempts && !reconnected) {
            System.out.println("Attempting to reconnect... (Attempt " + (attempts + 1) + ")");
            try {
                Thread.sleep(5000);
                javax.swing.SwingUtilities.invokeLater(() -> {
                  ui.getController().showBlockingDialogBox("Reconnecting... Please wait.");
                });

                if(!connectToWebSocket()){
                  javax.swing.SwingUtilities.invokeLater(() -> {
                    ui.getController().closeBlockingDialogBox();
                    ui.showErrorMessage("Failed to connect to WebSocket.");
                  });
                  throw new Exception("Failed to connect to WebSocket.");
                }
                // connected with socket
                if (session != null && session.isConnected()) {
                  if(!joinDocument(sessionCode)){
                      System.out.println("Failed to join document after reconnection.");
                      javax.swing.SwingUtilities.invokeLater(() -> {
                        ui.getController().closeBlockingDialogBox();
                        ui.showErrorMessage("Failed to join document after reconnection.");
                      });
                      break;
                  }
                    
                  reconnected = true;

                  for (int i = 0; i < bufferedOperations.size(); i++) {
                      sendOperations(bufferedOperations.get(i), true);
                      onSocketOperations(bufferedOperations.get(i), true);
                  }
                  bufferedOperations.clear();
              
                  // Notify the UI that all operations have been sent
                  javax.swing.SwingUtilities.invokeLater(() -> {
                      ui.getController().closeBlockingDialogBox();
                      ui.showMessage("Reconnected successfully!");
                      ui.getTopBarPanel().clearDisconnectedMark();
                      ui.getSidebarPanel().updateActiveUsers(Collections.emptyList());
                  });
                }
            } catch (Exception e) {
                System.out.println("Reconnection attempt failed: " + e.getMessage());
            }
            attempts++;
        }
        
        if (!reconnected) {
            // Final notification after all attempts fail
            javax.swing.SwingUtilities.invokeLater(() -> {
                ui.showErrorMessage("Could not reconnect after " + maxAttempts + " attempts");
            });
            ui.getMainPanel().removeDocument();
            this.documentID = null;
            this.codes = new HashMap<>();
            crdt = new CRDT();
            documentTitle = "null";
            isReader = false;
            undoStack.clear();
            redoStack.clear();
            sessionCode = "";
        }
        reconnecting = false;
    }).start();
  }

  public void sessionDisconnect() {
    if (session != null && session.isConnected()) {
      session.disconnect();
    }
  }

  public void simulateUnexpectedDisconnect() {
    if (session != null && session.isConnected()) {
        session.disconnect();
        handleUnexpectedDisconnect();
    }
}

  // setters and getters
  public void setUID(int UID) {
    this.UID = UID;
  }

  public List<Integer> getActiveUserIds() {
    return new ArrayList<>(activeUsers.keySet());
  }

  public List<Integer> getCursorPositions() {
    return new ArrayList<>(activeUsers.values());
  }

  public HashMap<Integer, Integer> getActiveUsers() {
    return activeUsers;
  }

  public int getUID() {
    return UID;
  }

  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  public String getDocumentID() {
    return documentID;
  }

  public CRDT getCrdt() {
    return crdt;
  }

  public CollaborativeUI getUI() {
    return ui;
  }

  public void sendOperations(List<Operation> operations) {
    sendOperations(operations, false);
  }

  public void sendOperations(List<Operation> operations, boolean noFollowers) {

    if(!noFollowers){
      for (Operation operation : operations) {
        Operation stackOperation = new Operation(operation.getOperationType(),
            operation.getUID(),
            operation.getTime(),
            operation.getValue(),
            operation.getParentId());
  
        if (operation.getSecondaryType() == SecondaryType.NORMAL) {
          stackOperation.setSecondaryType(SecondaryType.UNDO);
          if(operation.getOperationType() != OperationType.DELETE){
            stackOperation.setParentId(stackOperation.getID());
          }
          undoStack.push(stackOperation);
          redoStack.clear();
        } else if (operation.getSecondaryType() == SecondaryType.UNDO) {
          stackOperation.setSecondaryType(SecondaryType.REDO);
          redoStack.push(stackOperation);
        } else if (operation.getSecondaryType() == SecondaryType.REDO) {
          stackOperation.setSecondaryType(SecondaryType.UNDO);
          undoStack.push(stackOperation);
        }
        if(operation.getOperationType() == OperationType.PASTE) break; // only need the first node as its batch op
      }
    }

    if (session == null || !session.isConnected()) {
      bufferedOperations.add(operations);
      return;
    }

    session.send("/app/operations/" + documentID, operations);
  }

  public void sendCursor(Cursor cursor) {
    if (session != null && session.isConnected()) {
      session.send("/app/cursors/" + documentID, cursor);
    } else {
      System.out.println("Session is not connected. Cannot send cursor.");
    }
  }

  public boolean joinDocument(String sessionCode) {
    if (session != null && session.isConnected()) {
      try {
        // Send a request to get the documentID using the sessionCode
        URL url = new URL("http://localhost:8080/api/document/id/" + sessionCode);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
          try (BufferedReader br = new BufferedReader(
              new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            StringBuilder responseString = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
              responseString.append(responseLine.trim());
            }

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseString.toString());

            // Set the documentID in the client
            if (root.has("documentId")) {
              this.documentID = root.get("documentId").asText();
              codes = new HashMap<>();
              codes.put("readonlyCode", root.get("readonlycode").asText());
              isReader = !root.has("editorCode");
              if (!isReader) {
                codes.put("editorCode", root.get("editorCode").asText());
              }
              documentTitle = root.get("documentTitle").asText();
              crdt = CRDT.deserialize(root.get("crdt").asText());
              crdt.setPasteMapFromString(root.get("crdt_pasteMap").asText());
              ui.getMainPanel().displayDocument(crdt.toString(), documentTitle, isReader);
              this.sessionCode = sessionCode; 
              
              // set up listeners
              listen();

              Message message = new Message(UID, "join");
              session.send("/app/join/" + sessionCode, message);
              return true;
            } else {
              System.out.println("Error: " + root.get("error").asText());
              // use the ui to show the error message
              ui.showErrorMessage("no document found with this session code");
              return false;
            }
          }
        } else {
          System.out.println("Failed to get document ID. Response code: " + responseCode);
          return false;
        }
      } catch (Exception e) {
        System.out.println("Error joining document: " + e.getMessage());
        e.printStackTrace();
        return false;
      }
    } else {
      System.out.println("Session is not connected. Cannot join document.");
      return false;
    }
  }

  public void leaveDocument(String documentID) {
    if (session != null && session.isConnected()) {
      Message message = new Message(UID, "leave");
      session.send("/app/leave/" + documentID, message);
    } else {
      System.out.println("Session is not connected. Cannot leave document.");
    }
  }

  public HashMap<String, String> createDocument(String title) {
    return createDocument(title, null);
  }

  public HashMap<String, String> createDocument(String title, String initialContent) {
    HashMap<String, String> response = new HashMap<>();
    try {
      URL url = new URL("http://localhost:8080/api/document/create");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setDoOutput(true);

      // Use the provided title parameter instead of hardcoded "Untitled Document"
      String documentTitle = (title != null && !title.isEmpty()) ? title : "Untitled Document";

      // Create JSON payload including initial content if provided
      String jsonInputString;
      if (initialContent != null && !initialContent.isEmpty()) {
        jsonInputString = "{\"UID\":" + this.UID + ", \"name\":\"" + escapeJsonString(documentTitle)
            + "\", \"content\":\"" + escapeJsonString(initialContent) + "\"}";
      } else {
        jsonInputString = "{\"UID\":" + this.UID + ", \"name\":\"" + escapeJsonString(documentTitle) + "\"}";
      }

      try (OutputStream os = conn.getOutputStream()) {
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      // Rest of the method remains the same
      int responseCode = conn.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "utf-8"))) {
          StringBuilder responseString = new StringBuilder();
          String responseLine = null;
          while ((responseLine = br.readLine()) != null) {
            responseString.append(responseLine.trim());
          }

          // Parse JSON response
          ObjectMapper mapper = new ObjectMapper();
          JsonNode root = mapper.readTree(responseString.toString());

          response.put("docID", root.get("docID").asText());
          response.put("readonlyCode", root.get("readonlyCode").asText());
          response.put("editorCode", root.get("editorCode").asText());

          // // Set the document ID for this client correctly
          // this.documentID = response.get("docID");
          // this.codes.put("readonlyCode", response.get("readonlyCode"));
          // this.codes.put("editorCode", response.get("editorCode"));

        }
      } else {
        System.out.println("Failed to create document. Response code: " + responseCode);
      }

    } catch (Exception e) {
      System.out.println("Error creating document: " + e.getMessage());
      e.printStackTrace();
    }

    return response;
  }

  private String escapeJsonString(String input) {
    if (input == null)
      return "";

    return input.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\b", "\\b")
        .replace("\f", "\\f")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  public Operation getUndoLastOperation() {
    if (!undoStack.isEmpty()) {
      return undoStack.pop();
    } else {
      System.out.println("No operations to undo.");
      return null;
    }
  }

  public Operation getRedoLastOperation() {
    if (!redoStack.isEmpty()) {
      return redoStack.pop();
    } else {
      System.out.println("No operations to redo.");
      return null;
    }
  }

}
