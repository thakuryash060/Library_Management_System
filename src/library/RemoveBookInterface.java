package library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RemoveBookInterface {
    private JFrame frame;
    private JTextField isbnField, copiesField;
    private int librarianId;

    public RemoveBookInterface(int librarianId) {
        this.librarianId = librarianId;
        frame = new JFrame("Remove Book");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create a background panel with an image
        background_panel backgroundPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");
        backgroundPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel isbnLabel = new JLabel("ISBN:");
        isbnLabel.setForeground(Color.BLACK);
        isbnField = createStyledTextField();

        JLabel copiesLabel = new JLabel("Copies to Remove:");
        copiesLabel.setForeground(Color.BLACK);
        copiesField = createStyledTextField();

        JButton removeButton = createStyledButton("Remove Book");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeBookFromDatabase();
            }
        });

        // Adding components to the background panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        backgroundPanel.add(isbnLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        backgroundPanel.add(isbnField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        backgroundPanel.add(copiesLabel, gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        backgroundPanel.add(copiesField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        backgroundPanel.add(removeButton, gbc);

        // Add the background panel to the frame
        frame.add(backgroundPanel);
        frame.setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField(15);
        textField.setBackground(Color.WHITE); // Set background color
        textField.setForeground(Color.BLACK); // Set text color
        textField.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font style
        textField.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Set border style
        return textField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        // Set the button background and foreground colors
        Color buttonBackgroundColor = new Color(100, 149, 237); // Cornflower Blue
        Color buttonForegroundColor = Color.WHITE; // White

        button.setBackground(buttonBackgroundColor);
        button.setForeground(buttonForegroundColor);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 40)); // Set button size
        button.setFocusPainted(false); // Remove the default focus border
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add a border

        // Add mouse listener for hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonBackgroundColor.brighter()); // Change color on hover
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonBackgroundColor); // Change back to original color
            }
        });

        // Add tooltip for better user guidance
        button.setToolTipText("Click to remove the specified number of copies of the book");

        return button;
    }

    private void removeBookFromDatabase() {
        String isbn = isbnField.getText();
        int copiesToRemove;

        try {
            copiesToRemove = Integer.parseInt(copiesField.getText());
            if (copiesToRemove <= 0) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number of copies to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number of copies to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            String checkQuery = "SELECT available_copies FROM books WHERE isbn = ?";
            PreparedStatement checkStmt = con.prepareStatement(checkQuery);
            checkStmt.setString(1, isbn);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int availableCopies = rs.getInt("available_copies");
                if (availableCopies < copiesToRemove) {
                    JOptionPane.showMessageDialog(frame, "Not enough copies available to remove.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String updateQuery = "UPDATE books SET available_copies = available_copies - ? WHERE isbn = ?";
                PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                updateStmt.setInt(1, copiesToRemove);
                updateStmt.setString(2, isbn);

                int result = updateStmt.executeUpdate();
                if (result > 0) {
                    logAction(con, isbn, copiesToRemove, "removed");
                    JOptionPane.showMessageDialog(frame, "Book removed successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Error removing book from the database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "No book found with the given ISBN.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void logAction(Connection con, String isbn, int copies, String actionType) throws SQLException {
        String logQuery = "INSERT INTO librarian (id, copies, isbn, action_type) VALUES (?, ?, ?, ?)";
        PreparedStatement logStmt = con.prepareStatement(logQuery);
        logStmt.setInt(1, librarianId);
        logStmt.setInt(2, copies);
        logStmt.setString(3, isbn);
        logStmt.setString(4, actionType);
        logStmt.executeUpdate();
    }
}
