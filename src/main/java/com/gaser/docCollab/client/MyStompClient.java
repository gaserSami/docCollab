package com.gaser.docCollab.client;
import java.util.Collections;
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

import com.gaser.docCollab.UI.CollaborativeUI;
import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.websocket.Cursor;
import com.gaser.docCollab.websocket.Message;

import org.springframework.web.socket.sockjs.client.Transport;
import java.util.List;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MyStompClient {
  private StompSession session;
  private int UID;
  private String documentID;
  // TODO: HASH_MAP
  private ArrayList<Integer> activeUserIds;
  private ArrayList<Integer> cursorPositions;
  private CRDT crdt;
  private CollaborativeUI ui;

  public MyStompClient(int UID, CollaborativeUI ui) {
    this.UID = UID;
    this.ui = ui;
    this.crdt = new CRDT();
    this.activeUserIds = new ArrayList<>();
    this.cursorPositions = new ArrayList<>();
}

public void onUserJoin(int UID){
  activeUserIds.add(UID);
  cursorPositions.add(0); 
}

public void onUserLeave(int UID){
  for(int i = 0; i < activeUserIds.size(); i++) {
    if (activeUserIds.get(i).equals(UID)) {
      activeUserIds.remove(i);
      cursorPositions.remove(i);
      break;
    }
  }
}

public void onUserCursorChange(Cursor cursor) {
  int UID = cursor.getUID();
  int cursorPosition = cursor.getPos();

  for (int i = 0; i < activeUserIds.size(); i++) {
    if (activeUserIds.get(i).equals(UID)) {
      cursorPositions.set(i, cursorPosition);
      break;
    }
  }
}

public void listen(){
  String tmp = "/topic/operations/" + getDocumentID();
    System.out.println(tmp);

      session.subscribe("/topic/operations/" + getDocumentID(), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("received at /topic/operations/" + getDocumentID() );
                try {
                    if (payload instanceof String) {
                        String operation = (String) payload;
                       getCrdt().handleOperation(Operation.deserialize(operation));
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
        System.out.println("Client Subscribe to /topic/operations/"+getDocumentID());

        /////////////////
        session.subscribe("/topic/users/"+getDocumentID() , new StompFrameHandler() {
          @Override
          public Type getPayloadType(StompHeaders headers) {
              return Message.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
              try {
                System.out.println("received at /topic/" + getDocumentID() + "/users");
                  if (payload instanceof Message) {
                      Message message = (Message) payload;
                      Integer UID = message.getUID();
                      String content = message.getContent();
                      if("join".equals(content)){
                        onUserJoin(UID);
                        }else if("leave".equals(content)){
                            onUserLeave(UID);
                        }

                      getUI().getSidebarPanel().updateActiveUsers(
                      getActiveUserIds().stream()
                          .map(id -> "User " + id)
                          .collect(java.util.stream.Collectors.toList())
                      );
                      System.out.println("Received UID: " + UID.toString());
                  } else {
                      System.out.println("Received unexpected payload type: " + payload.getClass());
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
      System.out.println("Client Subscribe to /topic/users/"+getDocumentID());
      ///////////////////
      
      session.subscribe("/topic/cursors/" + getDocumentID(), new StompFrameHandler() {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Cursor.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("received at /topic/" +getDocumentID() + "/cursors");
            try {
                if (payload instanceof Cursor) {
                    Cursor cursor = (Cursor) payload;
                    onUserCursorChange(cursor);

                    getUI().getSidebarPanel().updateActiveUsers(
                    IntStream.range(0, getActiveUserIds().size())
                        .mapToObj(idx -> {
                            Integer id = getActiveUserIds().get(idx);
                            Integer position = getCursorPositions().get(id);
                            return "User " + id + "pos: " + position;
                        })
                        .collect(java.util.stream.Collectors.toList())
                    );
                    System.out.println("Received cursor: " + cursor.toString());
                } else {
                    System.out.println("Received unexpected payload type: " + payload.getClass());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    System.out.println("Client Subscribe to /topic/cursors/"+getDocumentID());
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
      activeUserIds.clear();
      cursorPositions.clear();
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
    return activeUserIds;
  }

  public List<Integer> getCursorPositions() {
    return cursorPositions;
  }

  public int getUID() {
    return UID;
  }

  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  public String getDocumentID() {
    return "1";
  }

  public CRDT getCrdt() {
    return crdt;
  }

  public CollaborativeUI getUI() {
    return ui;
  }

  public void sendOperation(Operation operation) {
    if (session != null && session.isConnected()) {
      session.send("/app/operations/" + documentID, operation.seralize());
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

public void joinDocument(String documentID) {
  this.documentID = documentID;
  if (session != null && session.isConnected()) {
    Message message = new Message(UID, "join");
    session.send("/app/users/" + documentID, message);
    System.out.println("Client Sent Join to /app/users/" + documentID);
  } else {
    System.out.println("Session is not connected. Cannot join document.");
  }
}

public void leaveDocument(String documentID) {
  if (session != null && session.isConnected()) {
    Message message = new Message(UID, "leave");
    session.send("/app/users/" + documentID, message);
    System.out.println("Client Sent Leave to /app/users/" + documentID);
  } else {
    System.out.println("Session is not connected. Cannot leave document.");
  }
}

}
