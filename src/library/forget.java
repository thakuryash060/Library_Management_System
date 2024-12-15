package library;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.Calendar;

public class forget {
    private JFrame frame2;
    private JTextField usernameField;
    private JButton nextButton;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> dayComboBox;

    public forget() {
        frame2 = new JFrame("Forgot Password");
        frame2.setSize(400, 400);
        frame2.setLocationRelativeTo(null);
        frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        background_panel bgPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");
        bgPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        bgPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 1;
        bgPanel.add(usernameField, gbc);

        JLabel lblDOB = new JLabel("Date of Birth:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        bgPanel.add(lblDOB, gbc);

        yearComboBox = new JComboBox<>();
        for (int year = Calendar.getInstance().get(Calendar.YEAR); year >= 1900; year--) {
            yearComboBox.addItem(year);
        }
        yearComboBox.addActionListener(e -> updateDays());
        gbc.gridx = 1;
        bgPanel.add(yearComboBox, gbc);

        monthComboBox = new JComboBox<>(new DateFormatSymbols().getMonths());
        monthComboBox.addActionListener(e -> updateDays());
        gbc.gridx = 2;
        bgPanel.add(monthComboBox, gbc);

        dayComboBox = new JComboBox<>();
        updateDays(); // Initialize with the correct number of days
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3; // Span across all columns
        bgPanel.add(dayComboBox, gbc);

        nextButton = createStyledButton("Next");
        nextButton.addActionListener(e -> handleNextAction()); // Add ActionListener to trigger password retrieval
        gbc.gridy = 3;
        bgPanel.add(nextButton, gbc);

        frame2.add(bgPanel);
        frame2.setVisible(true);
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

    private void updateDays() {
        int month = monthComboBox.getSelectedIndex(); // 0-based
        int year = (int) yearComboBox.getSelectedItem();

        dayComboBox.removeAllItems(); // Clear previous items

        int daysInMonth;
        switch (month) {
            case 1: // February
                daysInMonth = isLeapYear(year) ? 29 : 28;
                break;
            case 0: // January
            case 2: // March
            case 4: // May
            case 6: // July
            case 7: // August
            case 9: // October
            case 11: // December
                daysInMonth = 31;
                break;
            default: // April, June, September, November
                daysInMonth = 30;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            dayComboBox.addItem(day);
        }
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private void handleNextAction() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(frame2, "Please enter your username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int day = (int) dayComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1; // Months are zero-indexed
        int year = (int) yearComboBox.getSelectedItem();
        String dob = year + "-" + month + "-" + day; // Format for SQL query

        showPassword(username, dob);
    }

    private void showPassword(String username, String dob) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
                String query = "SELECT password FROM users WHERE username = ? AND dob = ?";
                PreparedStatement pst = con.prepareStatement(query);
                pst.setString(1, username);
                pst.setString(2, dob);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    String password = rs.getString("password");
                    JOptionPane.showMessageDialog(frame2, "Password: " + password);
                    frame2.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame2, "No matching record found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(frame2, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
