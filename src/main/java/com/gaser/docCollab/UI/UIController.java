package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.gaser.docCollab.server.Operation;
import com.gaser.docCollab.server.OperationType;
import com.gaser.docCollab.server.SecondaryType;
import com.gaser.docCollab.websocket.Cursor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.awt.*;

public class UIController {
    private CollaborativeUI ui;

    public UIController(CollaborativeUI ui) {
        this.ui = ui;
    }

    /**
     * Handles the join button click event
     */
    public void handleJoinButtonClick() {
        this.ui.getClient().disconnectFromWebSocket();

        // ui.getClient().setDocumentID(res.get("docID"));
        // ui.getClient().codes.put("readonlyCode", res.get("readonlyCode"));
        // ui.getClient().codes.put("editorCode", res.get("editorCode"));

        String sessionCode = ui.getTopBarPanel().getSessionCode();
        if (sessionCode.isEmpty()) {
            JOptionPane.showMessageDialog(ui, "Please enter a session code.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        handleJoin(sessionCode);
    }

    public void handleJoin(String sessionCode){
        ui.getClient().connectToWebSocket();
        ui.getClient().joinDocument(sessionCode);
    }

    /**
     * Handles text changes at the character level
     * 
     * @param character  The character that was added or removed
     * @param position   The position/index where the change occurred
     * @param isAddition True if character was added, false if removed
     */
    public void onCharacterChange(char character, int position, OperationType operationType) {
        if (operationType == OperationType.DELETE)
        position++;
        
        this.ui.getClient().incrementLamportTime();

        Operation operation = new Operation(
            operationType, ui.getClient().getUID(),
            this.ui.getClient().getLamportTime(),
            character,
            this.ui.getClient().getCrdt().getNodeFromPosition(position).getID());

        this.ui.getClient().sendOperations(java.util.Collections.singletonList(operation));
        this.ui.getClient().onSocketOperations(java.util.Collections.singletonList(operation), true); // local updates
    }

    public void onPaste(String text, int position) {
        this.ui.getClient().incrementLamportTime();

        java.util.List<Operation> operations = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            this.ui.getClient().incrementLamportTime();

            Operation operation = new Operation(
                OperationType.INSERT, ui.getClient().getUID(),
                this.ui.getClient().getLamportTime(),
                text.charAt(i),
                this.ui.getClient().getCrdt().getNodeFromPosition(position + i).getID());

            operations.add(operation);
        }

        this.ui.getClient().sendOperations(operations);
        this.ui.getClient().onSocketOperations(operations, true); // local updates
    }

    public void onCursorChange(int position) {
        this.ui.getClient().sendCursor(new Cursor(
                ui.getClient().getUID(),
                position));
    }

    /**
     * Handles the share button click event
     */
    public void handleShareButtonClick() {
        // Create a custom dialog for sharing options
        JDialog shareDialog = new JDialog(ui, "Share Document", true);
        shareDialog.setLayout(new BorderLayout());
        shareDialog.setSize(400, 300);
        shareDialog.setLocationRelativeTo(ui);

        // Create a panel for the sharing options with a title
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Share Access Codes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Create a panel for the access codes list
        JPanel codesPanel = new JPanel();
        codesPanel.setLayout(new BoxLayout(codesPanel, BoxLayout.Y_AXIS));
        codesPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Add sharing options - Editor access
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));

        JLabel editorLabel = new JLabel("Editor");
        editorLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Generate a mock editor code (would be dynamic in a real implementation)
        String editorCode = ui.getClient().codes.get("editorCode");
        if (editorCode == null) {
            editorCode = "No editor code available";
        }
        JTextField editorCodeField = new JTextField(editorCode);
        editorCodeField.setEditable(false);
        editorCodeField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        editorCodeField.setBackground(new Color(240, 240, 240));

        editorPanel.add(editorLabel, BorderLayout.NORTH);
        editorPanel.add(editorCodeField, BorderLayout.CENTER);

        // Add sharing options - Read-Only access
        JPanel readOnlyPanel = new JPanel(new BorderLayout());
        readOnlyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));

        JLabel readOnlyLabel = new JLabel("Read-Only");
        readOnlyLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Generate a mock read-only code
        String readOnlyCode = ui.getClient().codes.get("readonlyCode");
        if (readOnlyCode == null) {
            readOnlyCode = "No read-only code available";
        }
        JTextField readOnlyCodeField = new JTextField(readOnlyCode);
        readOnlyCodeField.setEditable(false);
        readOnlyCodeField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        readOnlyCodeField.setBackground(new Color(240, 240, 240));

        readOnlyPanel.add(readOnlyLabel, BorderLayout.NORTH);
        readOnlyPanel.add(readOnlyCodeField, BorderLayout.CENTER);

        // Add panels to the codes panel
        codesPanel.add(editorPanel);
        codesPanel.add(readOnlyPanel);

        // Add information text
        JLabel infoLabel = new JLabel("Share these codes with others to collaborate on this document.");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(15, 5, 0, 5));
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        // Add the components to the content panel
        contentPanel.add(codesPanel, BorderLayout.CENTER);
        contentPanel.add(infoLabel, BorderLayout.SOUTH);

        // Add a close button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> shareDialog.dispose());
        buttonPanel.add(closeButton);

        // Add panels to the dialog
        shareDialog.add(contentPanel, BorderLayout.CENTER);
        shareDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show the dialog
        shareDialog.setVisible(true);
    }

    /**
     * Generates a mock sharing code for demonstration
     * 
     * @return A random alphanumeric code
     */
    private String generateMockCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * Handles the file button click event
     */
    public void handleFileButtonClick() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("New")).addActionListener(e -> handleNewOption());
        menu.add(new JMenuItem("Import")).addActionListener(e -> handleImportOption());
        menu.add(new JMenuItem("Export")).addActionListener(e -> handleExportOption());
        menu.show(ui.getTopBarPanel().getFileButton(), 0, ui.getTopBarPanel().getFileButton().getHeight());
    }

    private void handleNewOption() {
        // Prompt user for document name
        String documentName = JOptionPane.showInputDialog(ui, 
                "Enter a name for the new document:", 
                "New Document", 
                JOptionPane.QUESTION_MESSAGE);
        
        // If user cancels or enters empty string, use default name
        if (documentName == null) {
            return; // User canceled the operation
        } else if (documentName.trim().isEmpty()) {
            documentName = "Untitled.txt";
        } else if (!documentName.toLowerCase().endsWith(".txt")) {
            // Add .txt extension if not present
            documentName = documentName + ".txt";
        }
        
        HashMap<String, String> res = ui.getClient().createDocument(documentName);
        
        if (res.isEmpty()) {
            JOptionPane.showMessageDialog(ui, 
                "Failed to create document. Please try again.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        ui.getClient().disconnectFromWebSocket();

        // ui.getClient().setDocumentID(res.get("docID"));
        // ui.getClient().codes.put("readonlyCode", res.get("readonlyCode"));
        // ui.getClient().codes.put("editorCode", res.get("editorCode"));

        handleJoin(res.get("editorCode"));
        
        // ui.getMainPanel().displayDocument("", documentName);
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(ui, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Handles the import option selection
     */
    /**
 * Handles the import option selection
 */
private void handleImportOption() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import Text File");
    fileChooser.setFileFilter(new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return "Text Files (*.txt)";
        }
    });

    int result = fileChooser.showOpenDialog(ui);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
            JOptionPane.showMessageDialog(ui,
                    "Only text (.txt) files are supported.",
                    "Invalid File Type",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Read the content of the file
            Scanner scanner = new Scanner(selectedFile);
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();

            // Check if content appears to be binary/non-text
            if (containsBinaryData(content.toString())) {
                JOptionPane.showMessageDialog(ui,
                        "The file doesn't appear to contain valid text content.",
                        "Invalid Content",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create a new document on the server
            HashMap<String, String> res = ui.getClient().createDocument(selectedFile.getName(), content.toString());
            // HashMap<String, String> res = ui.getClient().createDocument(selectedFile.getName(), "abc");
            
            if (res.isEmpty()) {
                JOptionPane.showMessageDialog(ui, 
                    "Failed to create document. Please try again.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Disconnect from any previous connection and join the new document
            ui.getClient().disconnectFromWebSocket();

            // ui.getClient().setDocumentID(res.get("docID"));
            // ui.getClient().codes.put("readonlyCode", res.get("readonlyCode"));
            // ui.getClient().codes.put("editorCode", res.get("editorCode"));

            handleJoin(res.get("editorCode"));
            
            // ui.getClient().getCrdt().fromString(content.toString());
            // Display the document with the imported content and filename
            // ui.getMainPanel().displayDocument(ui.getClient().getCrdt().toString(), selectedFile.getName());
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(ui,
                    "Could not open the file: " + e.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    }

    /**
     * Checks if the content contains binary data (non-text content)
     * 
     * @param content The content to check
     * @return true if binary data is detected
     */
    private boolean containsBinaryData(String content) {
        // A simple heuristic to detect binary data - look for null bytes or a high
        // ratio of non-printable characters
        if (content.contains("\0")) {
            return true;
        }

        int nonPrintableCount = 0;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c < 32 && c != '\n' && c != '\r' && c != '\t') {
                nonPrintableCount++;
            }
        }

        // If more than 10% of characters are non-printable, assume it's binary
        return nonPrintableCount > content.length() * 0.1;
    }

    /**
     * Handles the export option selection
     */
    private void handleExportOption() {
        MainDocumentPanel mainPanel = ui.getMainPanel();

        if (!mainPanel.hasOpenDocument()) {
            JOptionPane.showMessageDialog(ui,
                    "No document is currently open to export.",
                    "Export Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the current content
        String content = mainPanel.getDocumentContent();

        // Set up file chooser for saving
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Text File");

        // Try to get the current filename to suggest as default
        String currentFileName = mainPanel.getCurrentFileName();
        if (currentFileName != null && !currentFileName.isEmpty()) {
            fileChooser.setSelectedFile(new File(currentFileName));
        }

        // Set up file filter for txt files
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "Text Files (*.txt)";
            }
        });

        int result = fileChooser.showSaveDialog(ui);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Add .txt extension if not present
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
            }

            // Confirm overwrite if file exists
            if (selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(ui,
                        "The file already exists. Do you want to overwrite it?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try (PrintWriter writer = new PrintWriter(selectedFile)) {
                // Write content exactly as it appears in the text area
                writer.print(content);

                JOptionPane.showMessageDialog(ui,
                        "Document successfully exported to:\n" + selectedFile.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(ui,
                        "Could not save the file: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void handleUndoButtonClick() {
        Operation operation = this.ui.getClient().getUndoLastOperation();
        if (operation == null) {
            System.out.println("No operation to undo");
            return;
        }

        this.ui.getClient().incrementLamportTime();

        Operation operationToUndo = new Operation(
            operation.getOperationType(), ui.getClient().getUID(),
            this.ui.getClient().getLamportTime(),
            operation.getValue(),
            operation.getParentId()
            ,operation.getSecondaryType());

        this.ui.getClient().sendOperations(java.util.Collections.singletonList(operationToUndo));
        this.ui.getClient().onSocketOperations(java.util.Collections.singletonList(operationToUndo), true); // local updates
    }

    public void handleRedoButtonClick() {
        Operation operation = this.ui.getClient().getRedoLastOperation();
        if (operation == null) {
            System.out.println("No operation to redo");
            return;
        }

        this.ui.getClient().incrementLamportTime();

        Operation operationToRedo = new Operation(
            operation.getOperationType(), ui.getClient().getUID(),
            this.ui.getClient().getLamportTime(),
            operation.getValue(),
            operation.getParentId(),operation.getSecondaryType()
        );

        this.ui.getClient().sendOperations(java.util.Collections.singletonList(operationToRedo));
        this.ui.getClient().onSocketOperations(java.util.Collections.singletonList(operationToRedo), true); // local updates
    }
}