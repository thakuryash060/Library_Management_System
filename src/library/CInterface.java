package library;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class CInterface {

    private JFrame frame;
    private int userId;

    public CInterface(String username) {
        userId = getUserIdByUsername(username);

        frame = new JFrame("Student/Faculty - Library Access");
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        background_panel bgPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");
        bgPanel.setLayout(null);
        frame.add(bgPanel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel("Library Access - View & Request Books");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setBounds(0, 25, 800, 60);
        bgPanel.add(titleLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBounds(300, 150, 200, 250);
        JButton viewBooksButton = createStyledButton("View Available Books");
        viewBooksButton.addActionListener(e -> viewAvailableBooks());
        buttonPanel.add(viewBooksButton);

        JButton requestBorrowButton = createStyledButton("Request to Borrow Book");
        requestBorrowButton.addActionListener(e -> requestBorrowBook());
        buttonPanel.add(requestBorrowButton);

        JButton viewBorrowedButton = createStyledButton("View Borrowed Books");
        viewBorrowedButton.addActionListener(e -> viewBorrowedBooks());
        buttonPanel.add(viewBorrowedButton);

        JButton returnBookButton = createStyledButton("Return Borrowed Book");
        returnBookButton.addActionListener(e -> returnBook());
        buttonPanel.add(returnBookButton);

        // New logout button
        JButton logoutButton = createStyledButton("Logout");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);

        bgPanel.add(buttonPanel);

        frame.setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        return button;
    }

    private int getUserIdByUsername(String username) {
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

    private void viewAvailableBooks() {
        String[] columnNames = {
                "ID", "Title", "Author", "ISBN", "Genre", "Publisher",
                "Year", "Edition", "Language", "Available Copies", "Summary"
        };

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            String query = "SELECT book_id, title, author, isbn, genre, publisher, published_year, edition, language, available_copies, summary " +
                    "FROM books WHERE availability_status = 'available' AND available_copies > 0";
            PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("genre"),
                        rs.getString("publisher"),
                        rs.getInt("published_year"),
                        rs.getString("edition"),
                        rs.getString("language"),
                        rs.getInt("available_copies"),
                        rs.getString("summary")
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(25);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setSelectionBackground(new Color(173, 216, 230));
            table.setGridColor(Color.GRAY);

            JTableHeader header = table.getTableHeader();
            header.setBackground(new Color(70, 130, 180));
            header.setForeground(Color.WHITE);
            header.setFont(new Font("Arial", Font.BOLD, 15));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(800, 400));

            JOptionPane.showMessageDialog(frame, scrollPane, "Available Books", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void requestBorrowBook() {
        String isbn = JOptionPane.showInputDialog(frame, "Enter ISBN to Request Borrow:");
        if (isbn != null) {
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
                // Check if the book exists and has available copies
                String checkQuery = "SELECT book_id, available_copies FROM books WHERE isbn = ?";
                PreparedStatement checkPst = con.prepareStatement(checkQuery);
                checkPst.setString(1, isbn);
                ResultSet rs = checkPst.executeQuery();

                if (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    int availableCopies = rs.getInt("available_copies");

                    if (availableCopies > 0) {
                        // Insert borrow record without specifying due_date
                        String query = "INSERT INTO borrow_books (user_id, book_id, borrow_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
                        PreparedStatement pst = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                        pst.setInt(1, userId);
                        pst.setInt(2, bookId);
                        pst.executeUpdate();

                        // Update the available copies
                        String updateQuery = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?";
                        PreparedStatement updatePst = con.prepareStatement(updateQuery);
                        updatePst.setInt(1, bookId);
                        updatePst.executeUpdate();

                        // Retrieve the due_date from the database after insertion
                        ResultSet generatedKeys = pst.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int borrowId = generatedKeys.getInt(1);
                            String dueDateQuery = "SELECT due_date FROM borrow_books WHERE borrow_id = ?";
                            PreparedStatement dueDatePst = con.prepareStatement(dueDateQuery);
                            dueDatePst.setInt(1, borrowId);
                            ResultSet dueDateRs = dueDatePst.executeQuery();

                            if (dueDateRs.next()) {
                                LocalDate dueDate = dueDateRs.getDate("due_date").toLocalDate();
                                JOptionPane.showMessageDialog(frame, "Borrow request sent successfully. Due date is: " + dueDate);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "No copies available for this book.", "Unavailable", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "ISBN not found in the library database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewBorrowedBooks() {
        String[] columnNames = {"ID", "Title", "Due Date"};

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
            String query = "SELECT books.book_id, books.title, borrow_books.due_date " +
                    "FROM books JOIN borrow_books ON books.book_id = borrow_books.book_id " +
                    "WHERE borrow_books.user_id = ? AND borrow_books.return_date IS NULL";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();

            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getDate("due_date")
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(25);
            table.setFont(new Font("Arial", Font.PLAIN, 14));
            table.setSelectionBackground(new Color(173, 216, 230));
            table.setGridColor(Color.GRAY);

            JTableHeader header = table.getTableHeader();
            header.setBackground(new Color(70, 130, 180));
            header.setForeground(Color.WHITE);
            header.setFont(new Font("Arial", Font.BOLD, 15));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(600, 300));

            JOptionPane.showMessageDialog(frame, scrollPane, "Borrowed Books", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        String isbn = JOptionPane.showInputDialog(frame, "Enter ISBN to Return:");
        if (isbn != null) {
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
                boolean keepReturning = true;

                while (keepReturning) {
                    // Check if the user has an active borrowed copy of the book
                    String query = "SELECT bb.book_id FROM borrow_books bb JOIN books b ON bb.book_id = b.book_id " +
                            "WHERE bb.user_id = ? AND b.isbn = ? AND bb.return_date IS NULL";
                    PreparedStatement pst = con.prepareStatement(query);
                    pst.setInt(1, userId);
                    pst.setString(2, isbn);
                    ResultSet rs = pst.executeQuery();

                    if (rs.next()) {
                        int bookId = rs.getInt("book_id");

                        // Update the return date for one borrowed copy
                        String updateQuery = "UPDATE borrow_books SET return_date = CURRENT_TIMESTAMP " +
                                "WHERE user_id = ? AND book_id = ? AND return_date IS NULL LIMIT 1";
                        PreparedStatement updatePst = con.prepareStatement(updateQuery);
                        updatePst.setInt(1, userId);
                        updatePst.setInt(2, bookId);
                        updatePst.executeUpdate();

                        // Increase available copies by 1
                        String updateCopiesQuery = "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?";
                        PreparedStatement updateCopiesPst = con.prepareStatement(updateCopiesQuery);
                        updateCopiesPst.setInt(1, bookId);
                        updateCopiesPst.executeUpdate();

                        JOptionPane.showMessageDialog(frame, "Book returned successfully.");

                        // Check if there are more borrowed copies
                        query = "SELECT COUNT(*) AS remaining FROM borrow_books bb JOIN books b ON bb.book_id = b.book_id " +
                                "WHERE bb.user_id = ? AND b.isbn = ? AND bb.return_date IS NULL";
                        pst = con.prepareStatement(query);
                        pst.setInt(1, userId);
                        pst.setString(2, isbn);
                        rs = pst.executeQuery();

                        if (rs.next() && rs.getInt("remaining") > 0) {
                            int response = JOptionPane.showConfirmDialog(frame, "You have more copies of this book borrowed. Would you like to return another copy?", "Return Another Copy", JOptionPane.YES_NO_OPTION);
                            if (response != JOptionPane.YES_OPTION) {
                                keepReturning = false;
                            }
                        } else {
                            keepReturning = false;
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "You have not borrowed this book or it does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
                        keepReturning = false;
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void logout() {
        frame.dispose();
        new user_class();
    }
}
