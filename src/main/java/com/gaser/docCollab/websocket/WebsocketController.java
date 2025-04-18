package com.gaser.docCollab.websocket;
import com.gaser.docCollab.server.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class WebsocketController {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebsocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/users/{documentID}")
    public void onUserAction(@DestinationVariable String documentID, Message message) {
        System.out.println("recieved message at /users/" + documentID + " : " + message.getContent());
        messagingTemplate.convertAndSend("/topic/" + documentID + "/users" , message);
    }
    
    @MessageMapping("/operations/{documentID}")
    public void onSend(@DestinationVariable String documentID, Operation operation) {
        System.out.println("recieved message at /operations/" + documentID + " : " + operation.toString());
        messagingTemplate.convertAndSend("/topic/" + documentID + "/operations", operation);
    }
    
    @MessageMapping("/cursors/{documentID}")
    public void onCursor(@DestinationVariable String documentID, Cursor cursor) {
        System.out.println("recieved message at /cursors/" + documentID + " : " + cursor.toString());
        messagingTemplate.convertAndSend("/topic/" + documentID + "/cursors", cursor);
    }
}