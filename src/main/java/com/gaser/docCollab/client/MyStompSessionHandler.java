package com.gaser.docCollab.client;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.IntStream;

import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.websocket.Cursor;
import com.gaser.docCollab.websocket.Message;

public class MyStompSessionHandler extends StompSessionHandlerAdapter{
  private MyStompClient stompClient;

  public MyStompSessionHandler(MyStompClient stompClient) {
    this.stompClient = stompClient;
  }

  @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("Client Connected");

        session.subscribe("/topic/" + stompClient.getDocumentID() + "/operations", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Operation.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("received at /topic/" + stompClient.getDocumentID() + "/operations");
                try {
                    if (payload instanceof Operation) {
                        Operation operation = (Operation) payload;
                        stompClient.getCrdt().handleOperation(operation);
                        stompClient.getUI().getMainPanel().updateDocumentContent(stompClient.getCrdt().toString());
                        System.out.println("Received operation: " + operation.toString());
                    } else {
                        System.out.println("Received unexpected payload type: " + payload.getClass());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("Client Subscribe to /topic/operations/"+stompClient.getDocumentID());

        /////////////////
        session.subscribe("/topic/" + stompClient.getDocumentID() + "/users", new StompFrameHandler() {
          @Override
          public Type getPayloadType(StompHeaders headers) {
              return Message.class;
          }

          @Override
          public void handleFrame(StompHeaders headers, Object payload) {
              try {
                System.out.println("received at /topic/" + stompClient.getDocumentID() + "/users");
                  if (payload instanceof Message) {
                      Message message = (Message) payload;
                      Integer UID = message.getUID();
                      String content = message.getContent();
                      if("join".equals(content)){
                        stompClient.onUserJoin(UID);
                        }else if("leave".equals(content)){
                            stompClient.onUserLeave(UID);
                        }

                      stompClient.getUI().getSidebarPanel().updateActiveUsers(
                      stompClient.getActiveUserIds().stream()
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
      System.out.println("Client Subscribe to /topic/users/"+stompClient.getDocumentID());
      ///////////////////
      
      session.subscribe("/topic/" + stompClient.getDocumentID() + "/cursors", new StompFrameHandler() {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Cursor.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("received at /topic/" + stompClient.getDocumentID() + "/cursors");
            try {
                if (payload instanceof Cursor) {
                    Cursor cursor = (Cursor) payload;
                    stompClient.onUserCursorChange(cursor);

                    stompClient.getUI().getSidebarPanel().updateActiveUsers(
                    IntStream.range(0, stompClient.getActiveUserIds().size())
                        .mapToObj(idx -> {
                            Integer id = stompClient.getActiveUserIds().get(idx);
                            Integer position = stompClient.getCursorPositions().get(id);
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
    System.out.println("Client Subscribe to /topic/cursors/"+stompClient.getDocumentID());
    ///////////////////////////

      stompClient.joinDocument(stompClient.getDocumentID());
    }

  @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
    }
}
