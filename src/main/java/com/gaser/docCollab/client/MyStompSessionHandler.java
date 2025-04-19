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
    System.out.println("Client Connected");

  }

  @Override
  public void handleTransportError(StompSession session, Throwable exception) {
    exception.printStackTrace();
  }
}
