package com.gaser.docCollab.websocket;

import com.gaser.docCollab.client.CRDT;
import com.gaser.docCollab.server.Operation;

import java.util.List;
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
        String docID = res.get("docID");

        // Set up response with active users and CRDT state
        // message.setDocumentID(docID);
        message.setActiveUsers(webSocketService.getActiveUsers(docID));
        int docLampertTime = webSocketService.getLampertTime(docID);
        message.setLamportTime(docLampertTime); // for syncing up

        // Only add CRDT to the response when someone joins (not for other updates)
        // message.setCRDT(webSocketService.getCRDT(docID).serialize());

        // testing seraltion and deserialization

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

}