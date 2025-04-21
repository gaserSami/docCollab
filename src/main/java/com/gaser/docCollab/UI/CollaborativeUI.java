package com.gaser.docCollab.UI;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import com.gaser.docCollab.client.MyStompClient;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CollaborativeUI extends JFrame {
    private SidebarPanel sidebarPanel;
    private MainDocumentPanel mainPanel;
    private TopBarPanel topBarPanel;
    private UIController controller;
    private MyStompClient client;
    private int UID;

    public CollaborativeUI(int UID) {
        this.UID = UID;
        setTitle("Collaborative Tool" + " - UID: " + UID);
        setSize(1366, 768);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Add custom behavior here
                handleApplicationClose();
            }
        });
        setLayout(new BorderLayout());

        // Create controller
        controller = new UIController(this);

        // Initialize components
        initComponents();

        // Set up the layout
        layoutComponents();

        setVisible(true);
    }

    private void handleApplicationClose() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit? Any unsaved changes will be lost.",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Perform cleanup
            controller.getUI().getClient().disconnectFromWebSocket();
            // Exit the application
            dispose();
            System.exit(0);
        }
    }

    private void initComponents() {
        // Create modular components
        sidebarPanel = new SidebarPanel(UID);
        mainPanel = new MainDocumentPanel(controller);
        topBarPanel = new TopBarPanel(controller);
    }

    private void layoutComponents() {
        // Add components to the frame
        add(sidebarPanel, BorderLayout.WEST);
        add(topBarPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    // Getters for components
    public SidebarPanel getSidebarPanel() {
        return sidebarPanel;
    }

    public MainDocumentPanel getMainPanel() {
        return mainPanel;
    }

    public TopBarPanel getTopBarPanel() {
        return topBarPanel;
    }

    // Delegate methods
    public void updateSessionCode(String code) {
        topBarPanel.updateSessionCode(code);
    }

    public void setClient(MyStompClient client) {
        this.client = client;
    }

    public MyStompClient getClient() {
        return client;
    }

    public void showErrorMessage(String message) {
        controller.showErrorMessage(message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            int id = new Random().nextInt(1000);
            CollaborativeUI ui = new CollaborativeUI(id);
            MyStompClient client = new MyStompClient(id, ui);
            ui.setClient(client);
        });
    }
}