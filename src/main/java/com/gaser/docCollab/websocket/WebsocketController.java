package com.gaser.docCollab.websocket;

import com.gaser.docCollab.client.CRDT;
import com.gaser.docCollab.server.Operation;

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
        System.out.println("received message at /join/" + sessionCode + " : " + message.getContent());

        // Add user to document and get document info
        HashMap<String, String> res = webSocketService.joinDocument(message.getUID(), sessionCode);
        String docID = res.get("docID");

        // Set up response with active users and CRDT state
        // message.setDocumentID(docID);
        message.setActiveUsers(webSocketService.getActiveUsers(docID));

        // Only add CRDT to the response when someone joins (not for other updates)
        // message.setCRDT(webSocketService.getCRDT(docID).serialize());

        // testing seraltion and deserialization
        // System.out.println("seralized crdt: " + message.getCRDT());
        // System.out.println("deserialized crdt: " +
        // CRDT.deserialize(message.getCRDT()));

        // message.codes = new HashMap<>();
        // message.codes.put("readonlyCode", webSocketService.getReadOnlyCode(docID));
        // if(!message.isReader) message.codes.put("editorCode",
        // webSocketService.getEditorCode(docID));
        message.isReader = res.get("isReader").equals("true");
        message.setDocumentTitle(webSocketService.getDocument(docID).getTitle());

        // Broadcast to all clients including the sender
        messagingTemplate.convertAndSend("/topic/users/" + docID, message);
    }

    @MessageMapping("/leave/{docID}")
    public void onLeave(@DestinationVariable String docID, Message message) {
        System.out.println("recieved message at /leave/" + docID + " : " + message.getContent());
        webSocketService.leaveDocument(message.getUID(), docID);
        message.setActiveUsers(webSocketService.getActiveUsers(docID));

        System.out.println("user: " + message.getUID() + " left document: " + docID);
        System.out.println("active users: " + message.getActiveUsers().toString());
        messagingTemplate.convertAndSend("/topic/users/" + docID, message);
    }

    @MessageMapping("/operations/{documentID}")
    public void onSend(@DestinationVariable String documentID, Operation operation) {
        System.out.println("recieved message at /operations/" + documentID + " : " + operation.toString());
        webSocketService.handleOperation(documentID, operation);
        messagingTemplate.convertAndSend("/topic/operations/" + documentID, operation);
    }

    @MessageMapping("/cursors/{documentID}")
    public void onCursor(@DestinationVariable String documentID, Cursor cursor) {
        System.out.println("recieved message at /cursors/" + documentID + " : " + cursor.toString());
        webSocketService.handleCursorUpdate(documentID, cursor);
        messagingTemplate.convertAndSend("/topic/cursors/" + documentID, cursor);
    }

}