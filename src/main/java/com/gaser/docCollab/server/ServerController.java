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
        return webSocketService.createDocument(request.getUID(), request.getName(), request.getContent());
    }

    @GetMapping("/id/{sessionCode}")
    public HashMap<String, String> getDocumentIDFromSessionCode(@PathVariable String sessionCode) {
        HashMap<String, String> response = new HashMap<>();
        String documentId = webSocketService.getDocumentIDFromSessionCode(sessionCode);
        if (documentId != null) {
            // Lock the document during retrieval to prevent concurrent operations
            // This means the document will be locked when user joins
            webSocketService.incrementJoinCount(documentId);
            
            // Wait for any active operations to complete before proceeding
            waitForActiveOperations(documentId);
            
            response.put("documentId", documentId);
            boolean isReadCode = webSocketService.isReadCode(sessionCode);
            response.put("readonlycode", webSocketService.getReadOnlyCode(documentId));
            if (!isReadCode) {
                response.put("editorCode", webSocketService.getEditorCode(documentId));
            }
            response.put("crdt", webSocketService.getCRDT(documentId).serialize());
            response.put("crdt_pasteMap", webSocketService.getCRDT(documentId).getPasteMapAsString());
            response.put("documentTitle", webSocketService.getDocument(documentId).getTitle());
        } else {
            response.put("error", "Invalid session code");
        }
        return response;
    }
    
    /**
     * Endpoint to check if a session code is valid without retrieving document data
     * @param sessionCode The session code to validate
     * @return A HashMap with a boolean indicating if the code is valid
     */
    @GetMapping("/validate/{sessionCode}")
    public HashMap<String, Boolean> validateSessionCode(@PathVariable String sessionCode) {
        HashMap<String, Boolean> response = new HashMap<>();
        String documentId = webSocketService.getDocumentIDFromSessionCode(sessionCode);
        response.put("valid", documentId != null);
        return response;
    }
    
    /**
     * Waits for all active operations on the document to complete before proceeding
     * This ensures that document retrieval doesn't interfere with ongoing operations
     */
    private void waitForActiveOperations(String documentID) {
        while (webSocketService.hasActiveOperations(documentID)) {
            try {
                Thread.sleep(100); // Small delay to check again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
