package com.gaser.docCollab.client;

import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {
  private MyStompClient stompClient;
  private StompSession session;
  private Thread connectionMonitor;
  private AtomicBoolean monitorRunning = new AtomicBoolean(false);

  public MyStompSessionHandler(MyStompClient stompClient) {
    this.stompClient = stompClient;
  }

  @Override
  public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    this.session = session;
    stompClient.setHeaders(connectedHeaders);
    
    // Start the connection monitoring thread
    startConnectionMonitor();
  }

  private void startConnectionMonitor() {
    if (monitorRunning.get()) {
      return; // Monitor already running
    }
    
    monitorRunning.set(true);
    connectionMonitor = new Thread(() -> {
      try {
        // Check connection every 5 seconds
        while (monitorRunning.get() && !Thread.currentThread().isInterrupted()) {
          if (session != null && !session.isConnected()) {
            System.out.println("Connection monitor detected disconnection");
            stompClient.handleUnexpectedDisconnect();
            // Stop monitoring after disconnection detected
            monitorRunning.set(false);
            break;
          }
          Thread.sleep(5000); // 5 second interval
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        System.out.println("Connection monitor interrupted: " + e.getMessage());
      } catch (Exception e) {
        System.out.println("Error in connection monitor: " + e.getMessage());
        e.printStackTrace();
      } finally {
        monitorRunning.set(false);
      }
    });
    
    connectionMonitor.setDaemon(true); // Make it a daemon thread so it doesn't prevent JVM shutdown
    connectionMonitor.setName("STOMP-Connection-Monitor");
    connectionMonitor.start();
    System.out.println("Connection monitor started");
  }
  
  public void stopConnectionMonitor() {
    monitorRunning.set(false);
    if (connectionMonitor != null && connectionMonitor.isAlive()) {
      connectionMonitor.interrupt();
    }
  }

  @Override
  public void handleTransportError(StompSession session, Throwable exception) {
    System.out.println("Transport error occurred: " + exception.getMessage());
    exception.printStackTrace();
  }
}
