package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LibrarianInterface {

    private JFrame frame;
    private int librarianId;

    public LibrarianInterface(String username) {
        librarianId = getLibrarianIdByUsername(username);
        if (librarianId == -1 || !isLibrarian(librarianId)) {
            JOptionPane.showMessageDialog(null, "Access Denied: You do not have librarian privileges.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        frame = new JFrame("Librarian - Manage Books");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);

        // Background panel
        background_panel bgPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");
        bgPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Manage Books");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        titleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        bgPanel.add(titleLabel, gbc);

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 2, 15, 15)); // 2 rows, 2 columns, with spacing
        buttonsPanel.setOpaque(false);

        JButton addBookButton = createButton("Add Book", e -> new AddBookInterface(librarianId));
        JButton removeBookButton = createButton("Remove Book", e -> new RemoveBookInterface(librarianId));
        JButton viewAllBooksButton = createButton("All Book Details", e -> viewAllBooks());
        JButton viewRentedUsersButton = createButton("Users Who Rented Books", e -> viewRentedUsers());

        buttonsPanel.add(addBookButton);
        buttonsPanel.add(removeBookButton);
        buttonsPanel.add(viewAllBooksButton);
        buttonsPanel.add(viewRentedUsersButton);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        bgPanel.add(buttonsPanel, gbc);

        // Sign Out Button
        JButton signOutButton = createButton("Sign Out", e -> signOut());
        gbc.gridy = 2;
        bgPanel.add(signOutButton, gbc);

        frame.add(bgPanel);
        frame.setVisible(true);
    }

    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addActionListener(actionListener);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 150, 200));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });

        return button;
    }

    private int getLibrarianIdByUsername(String username) {
        int id = -1;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            String query = "SELECT id FROM users WHERE username = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return id;
    }

    private boolean isLibrarian(int librarianId) {
        boolean isLibrarian = false;
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            String query = "SELECT role FROM users WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, librarianId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                isLibrarian = "Librarian".equalsIgnoreCase(rs.getString("role"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return isLibrarian;
    }

    private void viewAllBooks() {
        JFrame booksFrame = new JFrame("All Book Details");
        booksFrame.setSize(800, 400);
        booksFrame.setLocationRelativeTo(null);

        String[] columnNames = {"ID", "Title", "Author", "ISBN", "Genre", "Available Copies"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            String query = "SELECT * FROM books"; // Assuming you have a table named 'books'
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("genre"),
                        rs.getInt("available_copies")
                };
                model.addRow(row);
            }

            JTable booksTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(booksTable);
            booksFrame.add(scrollPane);
            booksFrame.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewRentedUsers() {
        JFrame rentedUsersFrame = new JFrame("Users Who Rented Books");
        rentedUsersFrame.setSize(800, 400);
        rentedUsersFrame.setLocationRelativeTo(null);

        String[] columnNames = {"User", "Email", "Book Title", "Borrow Date", "Due Date", "Return Date", "Penalty"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            // Updated SQL query to join users, books, and borrow_books tables
            String query = "SELECT users.username, users.email, books.title, borrow_books.borrow_date, borrow_books.due_date, borrow_books.return_date, borrow_books.penalty " +
                    "FROM borrow_books " +
                    "JOIN users ON borrow_books.user_id = users.id " +
                    "JOIN books ON borrow_books.book_id = books.book_id";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("title"),
                        rs.getDate("borrow_date"),
                        rs.getDate("due_date"),
                        rs.getDate("return_date"),  // This retrieves the return date for the book
                        rs.getDouble("penalty")      // This retrieves the penalty for the book
                };
                model.addRow(row);
            }

            JTable rentedUsersTable = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(rentedUsersTable);
            rentedUsersFrame.add(scrollPane);
            rentedUsersFrame.setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to handle sign out
    private void signOut() {
        frame.dispose();
        new user_class();
    }
}
