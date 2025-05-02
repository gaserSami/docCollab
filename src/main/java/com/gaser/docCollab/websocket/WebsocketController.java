package com.gaser.docCollab.websocket;

import com.gaser.docCollab.client.CRDT;
import com.gaser.docCollab.server.Operation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class WebsocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private WebSocketService webSocketService;

    @Autowired
    public WebsocketController(SimpMessagingTemplate messagingTemplate, WebSocketService webSocketService) {
        this.messagingTemplate = messagingTemplate;
        this.webSocketService = webSocketService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        // Handle new connection
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("New WebSocket connection: " + sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // Handle disconnection
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Integer userId = (Integer) headerAccessor.getSessionAttributes().get("userId");
        String docId = (String) headerAccessor.getSessionAttributes().get("docId");

        if (userId != null && docId != null) {
            System.out.println("WebSocket disconnected: " + sessionId + " for user " + userId);
            // if the user is in the active usersList then handle the disconnection
            if(webSocketService.getActiveUsers(docId).containsKey(userId)) {
                handleUserDisconnect(userId, docId);
            }
        }
    }

    @MessageMapping("/join/{sessionCode}")
    public void onJoin(@DestinationVariable String sessionCode, Message message, SimpMessageHeaderAccessor headerAccessor) {
        // Add user to document and get document info
        HashMap<String, String> res = webSocketService.joinDocument(message.getUID(), sessionCode);
        String docID = res.get("docID");
        
        headerAccessor.getSessionAttributes().put("userId", message.getUID());
        headerAccessor.getSessionAttributes().put("docId", docID);
        
        // Document is already locked by ServerController when fetching document info
        // No need to wait for active operations here as it's handled by ServerController
        
        webSocketService.removeReconnectingUser(res.get("docID"), message.getUID()); // if the user was in the reconnecting list
        // the prev line does nothing if the user was not in the reconnecting list
        message.setActiveUsers(webSocketService.getActiveUsers(docID));
        int docLampertTime = webSocketService.getLampertTime(docID);
        message.setLamportTime(docLampertTime); // for syncing up
        message.isReader = res.get("isReader").equals("true");
        message.setDocumentTitle(webSocketService.getDocument(docID).getTitle());

        // Broadcast to all clients including the sender
        messagingTemplate.convertAndSend("/topic/users/" + docID, message);
        
        // Unlock the document now that the join process is complete
        webSocketService.unlockDocument(docID);
    }

    @MessageMapping("/leave/{docID}")
    public void onLeave(@DestinationVariable String docID, Message message) {
        webSocketService.leaveDocument(message.getUID(), docID);
        message.setActiveUsers(webSocketService.getActiveUsers(docID));

        messagingTemplate.convertAndSend("/topic/users/" + docID, message);
    }

    @MessageMapping("/operations/{documentID}")
    public void onSend(@DestinationVariable String documentID, List<Operation> operations) {
        // Instead of blocking on the document lock, this method now tracks that an operation is in progress
        try {
            // Check only for general document lock (join in progress)
            while (webSocketService.isDocumentLocked(documentID)) {
                Thread.sleep(50); // Wait briefly then check again
            }
            
            // Increment the active operations counter
            webSocketService.incrementOperationCount(documentID);
            
            webSocketService.setLampertTime(documentID,
                    Math.max(webSocketService.getLampertTime(documentID), operations.get(operations.size() - 1).getTime())
                            + 1);
            webSocketService.handleOperations(documentID, operations);
            messagingTemplate.convertAndSend("/topic/operations/" + documentID, operations);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Ensure the counter is decremented even if an exception occurs
            webSocketService.decrementOperationCount(documentID);
        }
    }

    @MessageMapping("/cursors/{documentID}")
    public void onCursor(@DestinationVariable String documentID, Cursor cursor) {
        webSocketService.handleCursorUpdate(documentID, cursor);
        messagingTemplate.convertAndSend("/topic/cursors/" + documentID, cursor);
    }

    // to be called when a user socket connection drops unexpectedly
    // not for normal leaving
    public void handleUserDisconnect(Integer UID, String docID){
         // user is still connected check on the socket connection
        // if (
        //     // no action needed
        //     return;
        // )

        // this is the firt part
        webSocketService.leaveDocument(UID, docID);
        webSocketService.addReconnectingUser(docID, UID);
        Message message = new Message(UID, ""); // any id it doesn't matter 
        message.setActiveUsers(webSocketService.getActiveUsers(docID));
        message.setReconnectingUsers(webSocketService.getReconnectingUsers(docID));
        messagingTemplate.convertAndSend("/topic/users/" + docID, message);

        // this is the second part
        // Simulate a delay to check if the user reconnects
        new Thread(() -> {
            try {
                Thread.sleep(50000); // TIMEOUT
                if(webSocketService.removeReconnectingUser(docID, UID)) // only if the user didn't connect till the timeout
                {
                    message.setReconnectingUsers(webSocketService.getReconnectingUsers(docID));
                    messagingTemplate.convertAndSend("/topic/users/" + docID, message); // to remove the user from the reconnecting list
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}