package com.gaser.docCollab.UI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class CollaborativeUI extends JFrame {
    private JPanel sidebarPanel;
    private JPanel mainPanel;
    private JPanel topBarPanel;
    private JList<String> activeUsersList;
    private DefaultListModel<String> usersListModel;
    private JTextField sessionCodeField;
    private JButton joinButton;
    private JButton shareButton;
    private JButton fileButton;
    private JButton undoButton;
    private JButton redoButton;
    
    public CollaborativeUI() {
        setTitle("Collaborative Tool");
        setSize(1366, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize components
        initComponents();
        
        // Set up the layout
        layoutComponents();
        
        // Add some initial data
        updateActiveUsers(Arrays.asList("User 1 (You)"));
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Sidebar components
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setBackground(new Color(230, 230, 235));
        sidebarPanel.setPreferredSize(new Dimension(150, getHeight()));
        
        JLabel activeUsersLabel = new JLabel("Active Users");
        activeUsersLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        usersListModel = new DefaultListModel<>();
        activeUsersList = new JList<>(usersListModel);
        activeUsersList.setCellRenderer(new ActiveUserRenderer());
        activeUsersList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Top bar components
        topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        // configurable colors
        Color buttonBlue = new Color(60, 120, 255);
        Color buttonTextColor = Color.WHITE;

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
        
        // Main panel
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
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
    
    private void layoutComponents() {
        // Set up the sidebar
        JPanel usersLabelPanel = new JPanel(new BorderLayout());
        usersLabelPanel.add(new JLabel("Active Users"), BorderLayout.WEST);
        usersLabelPanel.setBackground(new Color(230, 230, 235));
        usersLabelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        sidebarPanel.add(usersLabelPanel, BorderLayout.NORTH);
        sidebarPanel.add(new JScrollPane(activeUsersList), BorderLayout.CENTER);
        
        // Set up the top bar
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        navigationPanel.add(fileButton);  // Add file button first
        // Add some spacing between file button and undo/redo buttons
        navigationPanel.add(Box.createHorizontalStrut(20));
        navigationPanel.add(undoButton);
        navigationPanel.add(redoButton);
        navigationPanel.setOpaque(false);
        
        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sessionPanel.add(sessionCodeField);
        sessionPanel.add(joinButton);  // Move join button next to session code
        sessionPanel.setOpaque(false);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // Remove join button from here
        actionPanel.add(shareButton);
        actionPanel.setOpaque(false);
        
        topBarPanel.setLayout(new BorderLayout());
        topBarPanel.add(navigationPanel, BorderLayout.WEST);
        topBarPanel.add(sessionPanel, BorderLayout.CENTER);
        topBarPanel.add(actionPanel, BorderLayout.EAST);
        
        // Add components to the frame
        add(sidebarPanel, BorderLayout.WEST);
        add(topBarPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Updates the list of active users
     * @param users List of user names to display
     */
    public void updateActiveUsers(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            usersListModel.clear();
            for (String user : users) {
                usersListModel.addElement(user);
            }
        });
    }
    
    /**
     * Adds a new user to the active users list
     * @param username Username to add
     */
    public void addActiveUser(String username) {
        SwingUtilities.invokeLater(() -> {
            usersListModel.addElement(username);
        });
    }
    
    /**
     * Removes a user from the active users list
     * @param username Username to remove
     */
    public void removeActiveUser(String username) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < usersListModel.size(); i++) {
                if (usersListModel.get(i).equals(username) || 
                    usersListModel.get(i).startsWith(username + " ")) {
                    usersListModel.remove(i);
                    break;
                }
            }
        });
    }
    
    /**
     * Updates the session code displayed in the text field
     * @param code New session code
     */
    public void updateSessionCode(String code) {
        SwingUtilities.invokeLater(() -> {
            sessionCodeField.setText(code);
        });
    }
    
    /**
     * Sets a listener for the join button
     * @param listener ActionListener to handle join button clicks
     */
    public void setJoinButtonListener(ActionListener listener) {
        joinButton.addActionListener(listener);
    }
    
    /**
     * Sets a listener for the share button
     * @param listener ActionListener to handle share button clicks
     */
    public void setShareButtonListener(ActionListener listener) {
        shareButton.addActionListener(listener);
    }
    
    /**
     * Sets a listener for the file button
     * @param listener ActionListener to handle file button clicks
     */
    public void setFileButtonListener(ActionListener listener) {
        fileButton.addActionListener(listener);
    }
    
    /**
     * Sets a listener for the back button
     * @param listener ActionListener to handle back button clicks
     */
    public void setundoButtonListener(ActionListener listener) {
        undoButton.addActionListener(listener);
    }
    
    /**
     * Sets a listener for the forward button
     * @param listener ActionListener to handle forward button clicks
     */
    public void setredoButtonListener(ActionListener listener) {
        redoButton.addActionListener(listener);
    }
    
    /**
     * Custom renderer for the active users list
     */
    private class ActiveUserRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                                                    int index, boolean isSelected, 
                                                    boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            if (!isSelected) {
                label.setBackground(new Color(230, 230, 235));
                label.setForeground(Color.BLACK);
            }
            
            return label;
        }
    }
    
/**
 * Handles the join button click event
 */
private void handleJoinButtonClick() {
    System.out.println("Join button clicked");
    // Add your join functionality here
}

/**
 * Handles the share button click event
 */
private void handleShareButtonClick() {
    System.out.println("Share button clicked");
    
    // Create a custom dialog for sharing options
    JDialog shareDialog = new JDialog(this, "Share Document", true);
    shareDialog.setLayout(new BorderLayout());
    shareDialog.setSize(400, 300);
    shareDialog.setLocationRelativeTo(this);
    
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
        BorderFactory.createEmptyBorder(10, 5, 10, 5)
    ));
    
    JLabel editorLabel = new JLabel("Editor");
    editorLabel.setFont(new Font("Arial", Font.BOLD, 14));
    
    // Generate a mock editor code (would be dynamic in a real implementation)
    String editorCode = generateMockCode();
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
        BorderFactory.createEmptyBorder(10, 5, 10, 5)
    ));
    
    JLabel readOnlyLabel = new JLabel("Read-Only");
    readOnlyLabel.setFont(new Font("Arial", Font.BOLD, 14));
    
    // Generate a mock read-only code
    String readOnlyCode = generateMockCode();
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
private void handleFileButtonClick() {
    JPopupMenu menu = new JPopupMenu();
    menu.add(new JMenuItem("Import")).addActionListener(e -> handleImportOption());
    menu.add(new JMenuItem("Export")).addActionListener(e -> handleExportOption());
    menu.show(fileButton, 0, fileButton.getHeight());
}

/**
 * Handles the import option selection
 */
private void handleImportOption() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Import Text File");
    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
        @Override
        public boolean accept(java.io.File file) {
            return file.isDirectory() || file.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return "Text Files (*.txt)";
        }
    });

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        java.io.File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
            JOptionPane.showMessageDialog(this, 
                "Only text (.txt) files are supported.", 
                "Invalid File Type", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Read the content of the file
            java.util.Scanner scanner = new java.util.Scanner(selectedFile);
            StringBuilder content = new StringBuilder();
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();

            // Check if content appears to be binary/non-text
            if (containsBinaryData(content.toString())) {
                JOptionPane.showMessageDialog(this, 
                    "The file doesn't appear to contain valid text content.", 
                    "Invalid Content", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create document display in main panel
            displayDocument(content.toString(), selectedFile.getName());
            
            System.out.println("File imported: " + selectedFile.getName());
        } catch (java.io.FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "Could not open the file: " + e.getMessage(), 
                "File Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

/**
 * Checks if the content contains binary data (non-text content)
 * @param content The content to check
 * @return true if binary data is detected
 */
private boolean containsBinaryData(String content) {
    // A simple heuristic to detect binary data - look for null bytes or a high ratio of non-printable characters
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
 * Displays the document content in the main panel
 * @param content The document content
 * @param fileName The name of the file
 */
private void displayDocument(String content, String fileName) {
    mainPanel.removeAll();
    
    // Create a text area for editing
    JTextArea textArea = new JTextArea(content);
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    
    JScrollPane scrollPane = new JScrollPane(textArea);
    
    // Create a document header panel
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    
    JLabel fileNameLabel = new JLabel(fileName);
    fileNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
    
    headerPanel.add(fileNameLabel, BorderLayout.WEST);
    
    // Add components to main panel
    mainPanel.add(headerPanel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);
    
    mainPanel.revalidate();
    mainPanel.repaint();
}

/**
 * Handles the export option selection
 */
private void handleExportOption() {
    // Find the text area in the main panel (if it exists)
    JTextArea textArea = findTextAreaInMainPanel();
    
    if (textArea == null) {
        JOptionPane.showMessageDialog(this, 
            "No document is currently open to export.", 
            "Export Error", 
            JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Get the current content
    String content = textArea.getText();
    
    // Set up file chooser for saving
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export Text File");
    
    // Try to get the current filename to suggest as default
    String currentFileName = getCurrentFileName();
    if (currentFileName != null && !currentFileName.isEmpty()) {
        fileChooser.setSelectedFile(new java.io.File(currentFileName));
    }
    
    // Set up file filter for txt files
    fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
        @Override
        public boolean accept(java.io.File file) {
            return file.isDirectory() || file.getName().toLowerCase().endsWith(".txt");
        }

        @Override
        public String getDescription() {
            return "Text Files (*.txt)";
        }
    });
    
    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        java.io.File selectedFile = fileChooser.getSelectedFile();
        
        // Add .txt extension if not present
        if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
            selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".txt");
        }
        
        // Confirm overwrite if file exists
        if (selectedFile.exists()) {
            int response = JOptionPane.showConfirmDialog(this, 
                "The file already exists. Do you want to overwrite it?", 
                "Confirm Overwrite", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        try (java.io.PrintWriter writer = new java.io.PrintWriter(selectedFile)) {
            // Write content exactly as it appears in the text area
            writer.print(content);
            System.out.println("File exported: " + selectedFile.getName());
            
            JOptionPane.showMessageDialog(this, 
                "Document successfully exported to:\n" + selectedFile.getAbsolutePath(), 
                "Export Successful", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "Could not save the file: " + e.getMessage(), 
                "Export Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

/**
 * Finds the text area in the main panel (if it exists)
 * @return The JTextArea component or null if not found
 */
private JTextArea findTextAreaInMainPanel() {
    // Check if there's a scroll pane in the main panel (center position)
    Component centerComponent = findComponentInBorderLayout(mainPanel, BorderLayout.CENTER);
    if (centerComponent instanceof JScrollPane) {
        JScrollPane scrollPane = (JScrollPane) centerComponent;
        Component viewComponent = scrollPane.getViewport().getView();
        if (viewComponent instanceof JTextArea) {
            return (JTextArea) viewComponent;
        }
    }
    return null;
}

/**
 * Gets the current document filename from the header label (if it exists)
 * @return The current filename or null if not found
 */
private String getCurrentFileName() {
    // Check if there's a header panel in the main panel (north position)
    Component northComponent = findComponentInBorderLayout(mainPanel, BorderLayout.NORTH);
    if (northComponent instanceof JPanel) {
        JPanel headerPanel = (JPanel) northComponent;
        Component westComponent = findComponentInBorderLayout(headerPanel, BorderLayout.WEST);
        if (westComponent instanceof JLabel) {
            return ((JLabel) westComponent).getText();
        }
    }
    return null;
}

/**
 * Helper method to find a component in a container with BorderLayout
 * @param container The container to search in
 * @param position The BorderLayout position to look for
 * @return The component at the specified position or null if not found
 */
private Component findComponentInBorderLayout(Container container, String position) {
    if (container.getLayout() instanceof BorderLayout) {
        for (Component component : container.getComponents()) {
            BorderLayout layout = (BorderLayout) container.getLayout();
            Object constraints = layout.getConstraints(component);
            if (constraints != null && constraints.equals(position)) {
                return component;
            }
        }
    }
    return null;
}

/**
 * Handles the back button click event
 */
private void handleundoButtonClick() {
    System.out.println("undo button clicked");
    // Add your back functionality here
}

/**
 * Handles the forward button click event
 */
private void handleredoButtonClick() {
    System.out.println("redo button clicked");
    // Add your forward functionality here
}

/**
 * Simulates adding users for demonstration purposes
 */
private void simulateUserUpdates() {
    updateActiveUsers(Arrays.asList("User 1 (You)", "User 2", "User 3"));
}

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        CollaborativeUI ui = new CollaborativeUI();
        
        // Set up event listeners with proper method references
        ui.setJoinButtonListener(e -> ui.handleJoinButtonClick());
        ui.setShareButtonListener(e -> ui.handleShareButtonClick());
        ui.setFileButtonListener(e -> ui.handleFileButtonClick());
        ui.setundoButtonListener(e -> ui.handleundoButtonClick());
        ui.setredoButtonListener(e -> ui.handleredoButtonClick());
        
        // Example of updating users after delay
        new Timer(2000, e -> ui.simulateUserUpdates()).start();
    });
}
}
