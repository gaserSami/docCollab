package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transparent overlay panel that renders cursors for collaborative editing
 * independently from the text component itself.
 */
public class CursorOverlayPanel extends JPanel {
    private final JTextComponent textComponent;
    private final Map<Integer, Integer> userCursorPositions = new HashMap<>();

    public CursorOverlayPanel(JTextComponent textComponent) {
        this.textComponent = textComponent;
        setOpaque(false);  // Make panel transparent
    }

    public void updateCursorPositions(Map<Integer, Integer> cursorPositions) {
        this.userCursorPositions.clear();
        this.userCursorPositions.putAll(cursorPositions);
        repaint();  // Request redraw of the panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        // Get list of user IDs in the same order they would appear in the sidebar
        List<Integer> activeUserIds = new ArrayList<>(userCursorPositions.keySet());
        
        // Add new cursor indicators for each user
        for (Integer userId : userCursorPositions.keySet()) {
            Integer position = userCursorPositions.get(userId);
            if (position != null && position >= 0 && position <= textComponent.getText().length()) {
                try {
                    // Find the user's index in the activeUserIds list
                    int userIndex = activeUserIds.indexOf(userId);
                    
                    // Get color for this user using the same index as in sidebar
                    Color cursorColor;
                    String colorHex = com.gaser.docCollab.websocket.COLORS.getColor(userIndex % 4);
                    cursorColor = Color.decode(colorHex);
                    
                    // Get the rectangle for the position
                    Rectangle r = textComponent.modelToView2D(position).getBounds();
                    
                    // Draw a thin vertical line (2 pixels wide)
                    g2d.setColor(cursorColor);
                    g2d.fillRect(r.x, r.y, 2, r.height);
                    
                    // Optional: draw a small colored label with user ID above the cursor
                    // g2d.setFont(new Font("Arial", Font.BOLD, 10));
                    // g2d.drawString("U" + userId, r.x, Math.max(0, r.y - 2));
                    
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
        
        g2d.dispose();
    }
}