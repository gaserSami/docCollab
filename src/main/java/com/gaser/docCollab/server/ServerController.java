package com.gaser.docCollab.server;
import com.gaser.docCollab.websocket.WebSocketService;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document")
public class ServerController {

  @Autowired  
  private WebSocketService webSocketService;

  @PostMapping("/create")
  public HashMap<String, String> createDocument(@RequestBody DocumentCreationRequest request) {
      return webSocketService.createDocument(request.getUID(), request.getName());
  }

  @GetMapping("/id/{sessionCode}")
  public HashMap<String, String> getDocumentIDFromSessionCode(@PathVariable String sessionCode) {
      HashMap<String, String> response = new HashMap<>();
      String documentId = webSocketService.getDocumentIDFromSessionCode(sessionCode);
      if (documentId != null) {
          response.put("documentId", documentId);
      } else {
          response.put("error", "Invalid session code");
      }
      return response;
  }

}
