package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class MainDocumentPanel extends JPanel {
    private JTextArea textArea;
    private String currentFileName = "Untitled.txt";
    private JLabel fileNameLabel;
    
    public MainDocumentPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
    }
    
    /**
     * Displays the document content in the panel
     * @param content The document content
     * @param fileName The name of the file
     */
    public void displayDocument(String content, String fileName) {
        removeAll();
        
        this.currentFileName = fileName;
        
        // Create a text area for editing
        textArea = new JTextArea(content);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
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
     * @return The content as a string
     */
    public String getDocumentContent() {
        return textArea != null ? textArea.getText() : "";
    }
    
    /**
     * Gets the current document filename
     * @return The filename
     */
    public String getCurrentFileName() {
        return currentFileName;
    }
    
    /**
     * Checks if there is an open document
     * @return true if a document is open
     */
    public boolean hasOpenDocument() {
        return textArea != null;
    }
}