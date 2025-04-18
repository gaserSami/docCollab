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


    }

  @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
    }
}
