package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import com.gaser.docCollab.server.OperationType;

import java.awt.*;

public class MainDocumentPanel extends JPanel {
    private JTextArea textArea;
    private String currentFileName = "Untitled.txt";
    private JLabel fileNameLabel;
    private UIController controller;
    private boolean isLocalChange = true;

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

        // Add document listener to track changes
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (isLocalChange && controller != null) {
                    try {
                        int pos = e.getOffset();
                        String addedText = textArea.getText(pos, e.getLength());
                        for (int i = 0; i < addedText.length(); i++) {
                            controller.onCharacterChange(addedText.charAt(i), pos + i, OperationType.INSERT);
                            System.out.println("sent a character update at index :" + i);
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
                    controller.onCharacterChange(' ', pos, OperationType.DELETE);
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
     * 
     * @return The content as a string
     */
    public String getDocumentContent() {
        return textArea != null ? textArea.getText() : "";
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
    public void updateDocumentContent(String content) {
        if (textArea != null) {
            int currentCaretPosition = textArea.getCaretPosition();
            isLocalChange = false;
            textArea.setText(content);
            textArea.setCaretPosition(Math.min(currentCaretPosition, content.length()));
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