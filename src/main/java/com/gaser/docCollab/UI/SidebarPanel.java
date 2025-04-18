package com.gaser.docCollab.UI;

import com.gaser.docCollab.websocket.COLORS;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class SidebarPanel extends JPanel {
    private JList<String> activeUsersList;
    private DefaultListModel<String> usersListModel;

    public SidebarPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 230, 235));
        setPreferredSize(new Dimension(150, getHeight()));

        initComponents();
    }

    private void initComponents() {
        JLabel activeUsersLabel = new JLabel("Active Users");
        activeUsersLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usersListModel = new DefaultListModel<>();
        activeUsersList = new JList<>(usersListModel);
        activeUsersList.setCellRenderer(new ActiveUserRenderer());
        activeUsersList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel usersLabelPanel = new JPanel(new BorderLayout());
        usersLabelPanel.add(new JLabel("Active Users"), BorderLayout.WEST);
        usersLabelPanel.setBackground(new Color(230, 230, 235));
        usersLabelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        add(usersLabelPanel, BorderLayout.NORTH);
        add(new JScrollPane(activeUsersList), BorderLayout.CENTER);
    }

    /**
     * Updates the list of active users
     * 
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
     * 
     * @param username Username to add
     */
    public void addActiveUser(String username) {
        SwingUtilities.invokeLater(() -> {
            usersListModel.addElement(username);
        });
    }

    /**
     * Removes a user from the active users list
     * 
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
                // Use the COLORS class to set a different color for each user
                // Use modulo to cycle through available colors if there are more users than
                // colors
                String colorHex = COLORS.getColor(index % 4);
                label.setForeground(Color.decode(colorHex));
            }

            return label;
        }
    }
}