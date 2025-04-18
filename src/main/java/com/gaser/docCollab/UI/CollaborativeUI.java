package com.gaser.docCollab.UI;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import com.gaser.docCollab.client.MyStompClient;


public class CollaborativeUI extends JFrame {
    private SidebarPanel sidebarPanel;
    private MainDocumentPanel mainPanel;
    private TopBarPanel topBarPanel;
    private UIController controller;
    private MyStompClient client;
    
    public CollaborativeUI() {
        setTitle("Collaborative Tool");
        setSize(1366, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create controller
        controller = new UIController(this);
        
        // Initialize components
        initComponents();
        
        // Set up the layout
        layoutComponents();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Create modular components
        sidebarPanel = new SidebarPanel();
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
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            CollaborativeUI ui = new CollaborativeUI();
            MyStompClient client = new MyStompClient(new Random().nextInt(100) , ui);
            ui.setClient(client);
        });
    }
}