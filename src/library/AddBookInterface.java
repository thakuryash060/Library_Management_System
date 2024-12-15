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

public class AddBookInterface {
    private JFrame frame;
    private JTextField titleField, authorField, isbnField, genreField, publisherField,
            publishedYearField, editionField, languageField, pagesField,
            totalCopiesField, availableCopiesField, summaryField;
    private JComboBox<String> availabilityStatusField;
    private int librarianId;

    public AddBookInterface(int librarianId) {
        this.librarianId = librarianId;
        frame = new JFrame("Add Book");
        frame.setSize(600, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        background_panel bgPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");
        bgPanel.setLayout(new GridBagLayout());
        frame.add(bgPanel, BorderLayout.CENTER);

        titleField = createTextField("Title:", bgPanel, 0);
        authorField = createTextField("Author:", bgPanel, 1);
        isbnField = createTextField("ISBN:", bgPanel, 2);
        genreField = createTextField("Genre:", bgPanel, 3);
        publisherField = createTextField("Publisher:", bgPanel, 4);
        publishedYearField = createTextField("Published Year:", bgPanel, 5);
        editionField = createTextField("Edition:", bgPanel, 6);
        languageField = createTextField("Language:", bgPanel, 7);
        pagesField = createTextField("Pages:", bgPanel, 8);
        totalCopiesField = createTextField("Total Copies:", bgPanel, 9);
        availableCopiesField = createTextField("Available Copies:", bgPanel, 10);

        availabilityStatusField = new JComboBox<>(new String[]{"Available", "Not Available"});
        addComponent(bgPanel, new JLabel("Availability Status:"), 11, 0);
        addComponent(bgPanel, availabilityStatusField, 11, 1);

        summaryField = createTextField("Summary:", bgPanel, 12);

        JButton addButton = createStyledButton("Add Book");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOrUpdateBookInDatabase();
            }
        });
        addComponent(bgPanel, addButton, 13, 0, 2);

        frame.setVisible(true);
    }

    private JTextField createTextField(String label, JPanel panel, int row) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.BLACK);
        JTextField textField = new JTextField();
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.BLACK);
        addComponent(panel, lbl, row, 0);
        addComponent(panel, textField, row, 1);
        return textField;
    }

    private void addComponent(JPanel panel, Component component, int row, int col) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private void addComponent(JPanel panel, Component component, int row, int col, int width) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridwidth = width;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        // Replace these colors with those from your user_class
        Color buttonBackgroundColor = new Color(100, 149, 237);
        Color buttonForegroundColor = Color.WHITE;

        button.setBackground(buttonBackgroundColor);
        button.setForeground(buttonForegroundColor);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 40));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Add mouse listener for hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonBackgroundColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(buttonBackgroundColor);
            }
        });

        // Add tooltip
        button.setToolTipText("Click to add a new book");

        return button;
    }

    private void addOrUpdateBookInDatabase() {
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        String genre = genreField.getText();
        String publisher = publisherField.getText();
        int publishedYear = 0;
        int pages = 0;
        int totalCopies = 0;
        int availableCopies = 0;

        try {
            publishedYear = Integer.parseInt(publishedYearField.getText());
            pages = Integer.parseInt(pagesField.getText());
            totalCopies = Integer.parseInt(totalCopiesField.getText());
            availableCopies = Integer.parseInt(availableCopiesField.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(frame, "Please enter valid numbers for published year, pages, total copies, and available copies.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String availabilityStatus = (String) availabilityStatusField.getSelectedItem();
        String summary = summaryField.getText();

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {

            String checkQuery = "SELECT total_copies, available_copies FROM books WHERE isbn = ?";
            try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
                checkStmt.setString(1, isbn);
                ResultSet resultSet = checkStmt.executeQuery();

                if (resultSet.next()) {

                    int currentTotal = resultSet.getInt("total_copies");
                    int currentAvailable = resultSet.getInt("available_copies");

                    String updateQuery = "UPDATE books SET total_copies = ?, available_copies = ?, date_added = NOW() WHERE isbn = ?";
                    try (PreparedStatement updateStmt = con.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, currentTotal + totalCopies);
                        updateStmt.setInt(2, currentAvailable + availableCopies);
                        updateStmt.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(frame, "Book quantity updated successfully!");
                } else {

                    String insertQuery = "INSERT INTO books (title, author, isbn, genre, publisher, published_year, edition, language, pages, availability_status, total_copies, available_copies, summary, date_added) "
                            + "VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
                    try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, title);
                        insertStmt.setString(2, author);
                        insertStmt.setString(3, isbn);
                        insertStmt.setString(4, genre);
                        insertStmt.setString(5, publisher);
                        insertStmt.setInt(6, publishedYear);
                        insertStmt.setString(7, editionField.getText());
                        insertStmt.setString(8, languageField.getText());
                        insertStmt.setInt(9, pages);
                        insertStmt.setString(10, availabilityStatus);
                        insertStmt.setInt(11, totalCopies);
                        insertStmt.setInt(12, availableCopies);
                        insertStmt.setString(13, summary);

                        insertStmt.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Book added successfully!");
                    }
                }

                clearFields();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating or adding book to the database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        genreField.setText("");
        publisherField.setText("");
        publishedYearField.setText("");
        editionField.setText("");
        languageField.setText("");
        pagesField.setText("");
        totalCopiesField.setText("");
        availableCopiesField.setText("");
        summaryField.setText("");
    }
}
