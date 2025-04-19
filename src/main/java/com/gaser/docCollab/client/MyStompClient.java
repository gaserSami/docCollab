package com.gaser.docCollab.client;

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
import com.gaser.docCollab.websocket.Cursor;
import com.gaser.docCollab.websocket.Message;

import org.springframework.web.socket.sockjs.client.Transport;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MyStompClient {
  private StompSession session;
  private int UID;
  private String documentID;
  public HashMap<String, String> codes;
  private HashMap<Integer, Integer> activeUsers;
  private CRDT crdt;
  private CollaborativeUI ui;
  private int lamportTime = 0;

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
  //   activeUserIds.add(UID);
  //   cursorPositions.add(0);
  // }

  // public void onUserLeave(int UID) {
  //   for (int i = 0; i < activeUserIds.size(); i++) {
  //     if (activeUserIds.get(i).equals(UID)) {
  //       activeUserIds.remove(i);
  //       cursorPositions.remove(i);
  //       break;
  //     }
  //   }
  // }

  public void onUserCursorChange(Cursor cursor) {
    activeUsers.put(cursor.getUID(), cursor.getPos());
  }

  public void setActiveUsers(HashMap<Integer, Integer> activeUsers) {
    this.activeUsers = activeUsers;
  }

  private void listen() {
    String tmp = "/topic/operations/" + getDocumentID();
    System.out.println(tmp);

    session.subscribe("/topic/operations/" + getDocumentID(), new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return Operation.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("received at /topic/operations/" + getDocumentID());
        try {
          if (payload instanceof Operation) {
            Operation operation = (Operation) payload;
            if (operation.getUID() != getUID())
              lamportTime = Math.max(lamportTime, operation.getTime()) + 1;
            getCrdt().handleOperation(operation);
            String crdtString = getCrdt().toString();
            System.out.println(crdtString);
            getUI().getMainPanel().updateDocumentContent(crdtString);
            System.out.println("Received operation: " + operation.toString());
          } else {
            System.out.println("Received unexpected payload type: " + payload.getClass());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    System.out.println("Client Subscribe to /topic/operations/" + getDocumentID());

    /////////////////
    session.subscribe("/topic/users/" + getDocumentID(), new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        System.out.println("Fy get payload type aho");
        return Message.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("E7na fy el topic users");
        try {
          System.out.println("received at /topic/" + getDocumentID() + "/users");
          if (payload instanceof Message) {
            Message message = (Message) payload;
            Integer UID = message.getUID();
            String content = message.getContent();
            // if ("join".equals(content)) {
            //   onUserJoin(UID);
            // } else if ("leave".equals(content)) {
            //   onUserLeave(UID);
            // }
            
            setActiveUsers(message.getActiveUsers());

            if (getUID() == UID && "join".equals(content)) {
              crdt = CRDT.deserialize(message.getCRDT());
              String crdtString = "";
              if(crdt != null){
                crdtString = crdt.toString();
              }
              System.out.println("Initialized CRDT from server: " + crdtString);
              getUI().getMainPanel().displayDocument(crdtString, "tmp file name");
              // getUI().getMainPanel().updateDocumentContent(crdtString);
              codes = message.codes;
            }

            System.out.println("Active users: " + message.getActiveUsers().toString());

            getUI().getSidebarPanel().updateActiveUsers(
                IntStream.range(0, getActiveUserIds().size())
                    .mapToObj(idx -> {
                      Integer id = getActiveUserIds().get(idx);
                      Integer position = getCursorPositions().get(idx);
                      return "User " + id + " - " + position;
                    })
                    .collect(java.util.stream.Collectors.toList()));
            System.out.println("Received UID: " + UID.toString());
          } else {
            System.out.println("Received unexpected payload type: " + payload.getClass());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    System.out.println("Client Subscribe to /topic/users/" + getDocumentID());
    ///////////////////

    session.subscribe("/topic/cursors/" + getDocumentID(), new StompFrameHandler() {
      @Override
      public Type getPayloadType(StompHeaders headers) {
        return Cursor.class;
      }

      @Override
      public void handleFrame(StompHeaders headers, Object payload) {
        System.out.println("received at /topic/" + getDocumentID() + "/cursors");
        try {
          if (payload instanceof Cursor) {
            Cursor cursor = (Cursor) payload;
            onUserCursorChange(cursor);

            getUI().getSidebarPanel().updateActiveUsers(
                IntStream.range(0, getActiveUserIds().size())
                    .mapToObj(idx -> {
                      Integer id = getActiveUserIds().get(idx);
                      Integer position = getCursorPositions().get(idx);
                      return "User " + id + " - " + position;
                    })
                    .collect(java.util.stream.Collectors.toList()));
            System.out.println("Received cursor: " + cursor.toString());
          } else {
            System.out.println("Received unexpected payload type: " + payload.getClass());
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    System.out.println("Client Subscribe to /topic/cursors/" + getDocumentID());
    /////////////////////////
  }

  public void connectToWebSocket() {
    try {
      List<Transport> transports = Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()));
      SockJsClient sockJsClient = new SockJsClient(transports);
      WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
      stompClient.setMessageConverter(new MappingJackson2MessageConverter());

      StompSessionHandler sessionHandler = new MyStompSessionHandler(this);
      String url = "ws://localhost:8080/ws";

      session = stompClient.connectAsync(url, sessionHandler).get();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void disconnectFromWebSocket() {
    if (session != null && session.isConnected()) {
      try {
        // First leave the current document to notify other users
        leaveDocument(this.documentID);

        // Then disconnect the session
        session.disconnect();
        System.out.println("Disconnected from WebSocket for document: " + this.documentID);

        // Clear session reference
        session = null;

        // Clear active users and cursor positions
        activeUsers.clear();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // setters and getters
  public void setUID(int UID) {
    this.UID = UID;
  }

  public List<Integer> getActiveUserIds() {
    return activeUsers.keySet().stream()
        .sorted()
        .collect(java.util.stream.Collectors.toList());
  }

  public List<Integer> getCursorPositions() {
    return activeUsers.values().stream()
        .sorted()
        .collect(java.util.stream.Collectors.toList());
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

  public void sendOperation(Operation operation) {
    if (session != null && session.isConnected()) {
      session.send("/app/operations/" + documentID, operation);
      System.out.println("Client Sent Operation to /app/operations/" + documentID + " : " + operation.toString());
    } else {
      System.out.println("Session is not connected. Cannot send operation.");
    }
  }

  public void sendCursor(Cursor cursor) {
    if (session != null && session.isConnected()) {
      session.send("/app/cursors/" + documentID, cursor);
      System.out.println("Client Sent Cursor to /app/cursors/" + documentID + " : " + cursor.toString());
    } else {
      System.out.println("Session is not connected. Cannot send cursor.");
    }
  }

  public void joinDocument(String sessionCode) {
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
                        System.out.println("Joining document with ID: " + this.documentID);
                        
                        // set up listeners
                        listen();
                        
                        Message message = new Message(UID, "join");
                        session.send("/app/join/" + this.documentID, message);
                        System.out.println("Client sent join message to /app/join/" + this.documentID);
                    } else {
                        System.out.println("Error: " + root.get("error").asText());
                    }
                }
            } else {
                System.out.println("Failed to get document ID. Response code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error joining document: " + e.getMessage());
            e.printStackTrace();
        }
    } else {
        System.out.println("Session is not connected. Cannot join document.");
    }
}

  public void leaveDocument(String documentID) {
    if (session != null && session.isConnected()) {
      Message message = new Message(UID, "leave");
      session.send("/app/leave/" + documentID, message);
      System.out.println("Client Sent Leave to /app/users/" + documentID);
    } else {
      System.out.println("Session is not connected. Cannot leave document.");
    }
  }

  public HashMap<String, String> createDocument() {
    HashMap<String, String> response = new HashMap<>();
    try {
        URL url = new URL("http://localhost:8080/api/document/create");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        
        // Create JSON payload with correct UID field name
        String jsonInputString = "{\"UID\":" + this.UID + ", \"name\":\"Untitled Document\"}";
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
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
                
                // Set the document ID for this client correctly (fix typo)
                this.documentID = response.get("docID");
                this.codes.put("readonlyCode", response.get("readonlyCode"));
                this.codes.put("editorCode", response.get("editorCode"));
                
                System.out.println("Document created successfully with ID: " + this.documentID);
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

}
