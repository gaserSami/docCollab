package com.gaser.docCollab.client;

import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {
  private MyStompClient stompClient;

  public MyStompSessionHandler(MyStompClient stompClient) {
    this.stompClient = stompClient;
  }

  @Override
  public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    // do nothing
  }

  @Override
  public void handleTransportError(StompSession session, Throwable exception) {
    System.out.println("Transport error occurred: " + exception.getMessage());
    exception.printStackTrace();

    if (!session.isConnected()) {
      System.out.println("Session disconnected unexpectedly");
      
      stompClient.handleUnexpectedDisconnect();
    }
  }
}
