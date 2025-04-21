package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;

public class TopBarPanel extends JPanel {
    private JTextField sessionCodeField;
    private JButton joinButton;
    private JButton shareButton;
    private JButton fileButton;
    private JButton undoButton;
    private JButton redoButton;
    private JButton disconnectButton;
    private UIController controller;

    public TopBarPanel(UIController controller) {
        this.controller = controller;
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        setLayout(new BorderLayout());

        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        // Configurable colors
        Color buttonBlue = new Color(60, 120, 255);
        Color buttonTextColor = Color.WHITE;
        Color disconnectRed = new Color(220, 60, 60);

        fileButton = new JButton("File");
        styleButton(fileButton, buttonBlue, buttonTextColor);

        undoButton = new JButton("↺");
        redoButton = new JButton("↻");

        sessionCodeField = new JTextField(15);
        sessionCodeField.setText("Session Code");

        joinButton = new JButton("Join");
        styleButton(joinButton, buttonBlue, buttonTextColor);

        shareButton = new JButton("Share");
        styleButton(shareButton, buttonBlue, buttonTextColor);

        disconnectButton = new JButton("Disconnect");
        styleButton(disconnectButton, disconnectRed, buttonTextColor);

        // Set listeners
        fileButton.addActionListener(e -> controller.handleFileButtonClick());
        undoButton.addActionListener(e -> controller.handleUndoButtonClick());
        redoButton.addActionListener(e -> controller.handleRedoButtonClick());
        joinButton.addActionListener(e -> controller.handleJoinButtonClick());
        shareButton.addActionListener(e -> controller.handleShareButtonClick());
        disconnectButton.addActionListener(e -> controller.handleDisconnectButtonClick());
    }

    private void layoutComponents() {
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navigationPanel.add(fileButton);
        navigationPanel.add(Box.createHorizontalStrut(20));
        navigationPanel.add(undoButton);
        navigationPanel.add(redoButton);
        navigationPanel.setOpaque(false);

        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sessionPanel.add(sessionCodeField);
        sessionPanel.add(joinButton);
        sessionPanel.setOpaque(false);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(disconnectButton);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(shareButton);
        actionPanel.setOpaque(false);

        add(navigationPanel, BorderLayout.WEST);
        add(sessionPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.EAST);
    }

    /**
     * Applies consistent styling to buttons to ensure colors display properly
     */
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    /**
     * Updates the session code displayed in the text field
     * 
     * @param code New session code
     */
    public void updateSessionCode(String code) {
        SwingUtilities.invokeLater(() -> {
            sessionCodeField.setText(code);
        });
    }

    public String getSessionCode() {
        return sessionCodeField.getText();
    }

    /**
     * Gets the file button
     * 
     * @return The file button
     */
    public JButton getFileButton() {
        return fileButton;
    }

    /**
     * Gets the join button
     * 
     * @return The join button
     */
    public JButton getJoinButton() {
        return joinButton;
    }

    /**
     * Gets the share button
     * 
     * @return The share button
     */
    public JButton getShareButton() {
        return shareButton;
    }

    /**
     * Gets the undo button
     * 
     * @return The undo button
     */
    public JButton getUndoButton() {
        return undoButton;
    }

    /**
     * Gets the redo button
     * 
     * @return The redo button
     */
    public JButton getRedoButton() {
        return redoButton;
    }

    public JButton getDisconnectButton() {
        return disconnectButton;
    }

    public void markDisconnected() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            String currentTitle = frame.getTitle();
            if (!currentTitle.contains("(Disconnected)")) {
                frame.setTitle(currentTitle + " (Disconnected)");
            }
        }
    }

    public void clearDisconnectedMark() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (frame != null) {
            String currentTitle = frame.getTitle();
            frame.setTitle(currentTitle.replace(" (Disconnected)", ""));
        }
    }
}