package com.gaser.docCollab.websocket;

import com.gaser.docCollab.client.CRDT;
import com.gaser.docCollab.server.Operation;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private WebSocketService webSocketService;

    @Autowired
    public WebsocketController(SimpMessagingTemplate messagingTemplate, WebSocketService webSocketService) {
        this.messagingTemplate = messagingTemplate;
        this.webSocketService = webSocketService;
    }

    @MessageMapping("/join/{sessionCode}")
    public void onJoin(@DestinationVariable String sessionCode, Message message) {
        // Add user to document and get document info
        HashMap<String, String> res = webSocketService.joinDocument(message.getUID(), sessionCode);
        webSocketService.removeReconnectingUser(res.get("docID"), message.getUID()); // if the user was in the reconnecting list

        String docID = res.get("docID");
        message.setActiveUsers(webSocketService.getActiveUsers(docID));
        int docLampertTime = webSocketService.getLampertTime(docID);
        message.setLamportTime(docLampertTime); // for syncing up
        message.isReader = res.get("isReader").equals("true");
        message.setDocumentTitle(webSocketService.getDocument(docID).getTitle());

        // Broadcast to all clients including the sender
        messagingTemplate.convertAndSend("/topic/users/" + docID, message);
    }

    @MessageMapping("/leave/{docID}")
    public void onLeave(@DestinationVariable String docID, Message message) {
        webSocketService.leaveDocument(message.getUID(), docID);
        message.setActiveUsers(webSocketService.getActiveUsers(docID));

        messagingTemplate.convertAndSend("/topic/users/" + docID, message);
    }

    @MessageMapping("/operations/{documentID}")
    public void onSend(@DestinationVariable String documentID, List<Operation> operations) {
        webSocketService.setLampertTime(documentID,
                Math.max(webSocketService.getLampertTime(documentID), operations.get(operations.size() - 1).getTime())
                        + 1);
        webSocketService.handleOperations(documentID, operations);
        messagingTemplate.convertAndSend("/topic/operations/" + documentID, operations);
    }

    @MessageMapping("/cursors/{documentID}")
    public void onCursor(@DestinationVariable String documentID, Cursor cursor) {
        webSocketService.handleCursorUpdate(documentID, cursor);
        messagingTemplate.convertAndSend("/topic/cursors/" + documentID, cursor);
    }

    // to be called when a user socket connection drops unexpectedly
    // not for normal leaving
    public void handleUserDisconnect(Integer UID, String docID){
        if () { // user is still connected
            // no action needed
            return;
        }

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
                Thread.sleep(5000); // Wait for 5 seconds (TIMEOUT)
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