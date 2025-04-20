package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.Color;

import com.gaser.docCollab.server.OperationType;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;

public class MainDocumentPanel extends JPanel {
    private JTextArea textArea;
    private String currentFileName = "Untitled.txt";
    private JLabel fileNameLabel;
    private UIController controller;
    private boolean isLocalChange = true;
    private HashMap<Integer, Integer> userCursorPositions = new HashMap<>();
    private HashMap<Integer, Object> userCursorHighlights = new HashMap<>();

    public MainDocumentPanel(UIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }

    public void displayDocument(String content, String fileName) {
        displayDocument(content, fileName, false);
    }

    /**
     * Displays the document content in the panel
     * 
     * @param content  The document content
     * @param fileName The name of the file
     */
    public void displayDocument(String content, String fileName, boolean isReader) {
        removeAll();

        this.currentFileName = fileName;

        // Create a text area for editing
        textArea = new JTextArea(content);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.setEditable(!isReader);

        // Add a visual indicator if in read-only mode
        if (isReader) {
            textArea.setBackground(new Color(245, 245, 245)); // Light gray background for read-only
        }

        // Override the paste action to detect paste events
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();

        // Store the original paste action
        final Action defaultPasteAction = actionMap.get("paste");

        // Create a custom paste action that calls onPaste
        actionMap.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get clipboard content
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    String pastedText = (String) clipboard.getData(DataFlavor.stringFlavor);
                    int caretPosition = textArea.getCaretPosition();

                    // // Temporarily disable local change tracking before pasting
                    // isLocalChange = false;

                    // // Execute the default paste action
                    // defaultPasteAction.actionPerformed(e);

                    // // Re-enable local change tracking after pasting completes
                    // isLocalChange = true;

                    // Call onPaste function with the clipboard content and caret position
                    if (controller != null) {
                        controller.onPaste(pastedText, caretPosition);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add document listener to track changes
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (isLocalChange && controller != null) {
                    try {
                        int pos = e.getOffset();
                        String addedText = textArea.getText(pos, e.getLength());
                        for (int i = 0; i < addedText.length(); i++) {
                            char character = addedText.charAt(i);
                            controller.onCharacterChange(character, pos + i, OperationType.INSERT);
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isLocalChange && controller != null) {
                    int pos = e.getOffset();
                    int length = e.getLength();
                    for (int i = 0; i < length; i++) {
                        controller.onCharacterChange(' ', pos, OperationType.DELETE);
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Plain text components do not fire these events
            }
        });

        // Add caret listener to track cursor position
        textArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (isLocalChange && controller != null) {
                    controller.onCursorChange(e.getDot());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(textArea);

        // Create a document header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        fileNameLabel = new JLabel(fileName);
        fileNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(fileNameLabel, BorderLayout.WEST);

        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    /**
     * Gets the current document content
     * P
     * 
     * @return The content as a string
     */
    public String getDocumentContent() {
        return textArea != null ? textArea.getText() : "";
    }

    public void updateCursorPositions(HashMap<Integer, Integer> cursorPositions) {
        if (textArea != null) {
            // Replace the current cursor positions with the new ones
            this.userCursorPositions = new HashMap<>(cursorPositions);

            // Update the highlights on the EDT to avoid threading issues
            SwingUtilities.invokeLater(() -> {
                drawCursors();
            });
        }
    }

    private void drawCursors() {
        // Clear existing highlights
        if (textArea != null) {
            DefaultHighlighter highlighter = (DefaultHighlighter) textArea.getHighlighter();
            
            // Remove all previous cursor highlights
            for (Object tag : userCursorHighlights.values()) {
                highlighter.removeHighlight(tag);
            }
            userCursorHighlights.clear();
            
            // Add new highlights for each user cursor
            for (Integer userId : userCursorPositions.keySet()) {
                Integer position = userCursorPositions.get(userId);
                if (position != null && position >= 0 && position <= textArea.getText().length()) {
                    try {
                        // Get color for this user (cycling through available colors)
                        String colorHex = com.gaser.docCollab.websocket.COLORS.getColor(userId % 4);
                        Color cursorColor = Color.decode(colorHex);
                        
                        // Create a custom painter for vertical cursor line
                        DefaultHighlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(cursorColor);
                        
                        // Add the highlight (a 1-pixel width line at the cursor position)
                        Object tag = highlighter.addHighlight(position, position, painter);
                        userCursorHighlights.put(userId, tag);
                        
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Gets the current document filename
     * 
     * @return The filename
     */
    public String getCurrentFileName() {
        return currentFileName;
    }

    /**
     * Checks if there is an open document
     * 
     * @return true if a document is open
     */
    public boolean hasOpenDocument() {
        return textArea != null;
    }

    /**
     * Updates the document content from remote changes
     * 
     * @param content The new content
     */
    public void updateDocumentContent(String content, boolean forced) { // forced is for the case
        // en lw ana ele paktb
        if (textArea != null) {
            int diff = Math.abs(content.length() - textArea.getText().length());
            int currentCaretPosition = textArea.getCaretPosition();
            String currentText = textArea.getText();
            boolean isInsertion = content.length() > currentText.length();

            if (diff >= 1) {
                if (isInsertion) {
                    int i = 0;
                    for (; i < currentText.length(); i++) {
                        if (content.charAt(i) != currentText.charAt(i)) {
                            if (i < currentCaretPosition || (forced && i == currentCaretPosition)) {
                                currentCaretPosition += diff;
                            }
                            break;
                        }
                    }
                    if (forced && i == currentText.length() && currentCaretPosition == currentText.length())
                        currentCaretPosition += diff;
                } else {
                    int i = 0;
                    for (; i < content.length(); i++) {
                        if (content.charAt(i) != currentText.charAt(i)) {
                            if (i < currentCaretPosition) {
                                currentCaretPosition -= diff;
                            }
                            break;
                        }
                    }
                    if (i == content.length() && currentCaretPosition > content.length())
                        currentCaretPosition = content.length();
                }
            }

            isLocalChange = false;
            textArea.setText(content);
            // Make sure the adjusted caret position is valid
            textArea.setCaretPosition(Math.min(Math.max(0, currentCaretPosition), content.length()));
            isLocalChange = true;
        }
    }

    /**
     * Updates document at specific position (for collaborative editing)
     * 
     * @param character The character to insert
     * @param position  The position to insert at
     */
    public void insertCharacter(char character, int position) {
        if (textArea != null) {
            try {
                isLocalChange = false;
                textArea.getDocument().insertString(position, String.valueOf(character), null);
                isLocalChange = true;
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes character at specific position (for collaborative editing)
     * 
     * @param position The position to remove from
     */
    public void removeCharacter(int position) {
        if (textArea != null) {
            try {
                isLocalChange = false;
                textArea.getDocument().remove(position, 1);
                isLocalChange = true;
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
}