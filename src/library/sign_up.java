package library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.regex.Pattern;

public class sign_up {
    private JFrame frame1;
    private JTextField usernameField, emailField, phoneField, nameField;
    private JPasswordField passwordField;
    private JComboBox<Integer> yearComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> dayComboBox;
    private JComboBox<String> roleComboBox;

    public sign_up() {
        frame1 = new JFrame("Sign Up");
        background_panel bgPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");

        frame1.setSize(500, 700);
        frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame1.setLocationRelativeTo(null);
        frame1.setContentPane(bgPanel);
        frame1.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("SIGN UP", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame1.add(titleLabel, gbc);

        addLabelAndField("Name:", nameField = createStyledTextField(), gbc, 1);
        addLabelAndField("Username:", usernameField = createStyledTextField(), gbc, 2);
        addLabelAndField("Password:", passwordField = createStyledPasswordField(), gbc, 3);
        addLabelAndField("Email ID:", emailField = createStyledTextField(), gbc, 4);

        JLabel lblDOB = new JLabel("Date of Birth:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        frame1.add(lblDOB, gbc);

        yearComboBox = new JComboBox<>();
        for (int year = Calendar.getInstance().get(Calendar.YEAR); year >= 1900; year--) {
            yearComboBox.addItem(year);
        }
        yearComboBox.addActionListener(e -> updateDays());
        gbc.gridx = 1;
        frame1.add(yearComboBox, gbc);

        monthComboBox = new JComboBox<>(new DateFormatSymbols().getMonths());
        monthComboBox.addActionListener(e -> updateDays());
        gbc.gridx = 2;
        frame1.add(monthComboBox, gbc);

        dayComboBox = new JComboBox<>();
        updateDays();
        gbc.gridx = 3;
        frame1.add(dayComboBox, gbc);

        addLabelAndField("Role:", roleComboBox = new JComboBox<>(new String[]{"Student", "Faculty", "Librarian"}), gbc, 6);
        addLabelAndField("Phone:", phoneField = createStyledTextField(), gbc, 7);

        JButton btnCreateAccount = new JButton("Create Account");
        btnCreateAccount.setBackground(new Color(70, 130, 180));
        btnCreateAccount.setForeground(Color.WHITE);
        btnCreateAccount.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        frame1.add(btnCreateAccount, gbc);

        btnCreateAccount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    createUserAccount();
                }
            }
        });

        frame1.setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        textField.setPreferredSize(new Dimension(200, 30));
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        return textField;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        return passwordField;
    }

    private void addLabelAndField(String labelText, JTextField field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        frame1.add(label, gbc);

        gbc.gridx = 1;
        frame1.add(field, gbc);
    }

    private void addLabelAndField(String labelText, JComboBox<String> field, GridBagConstraints gbc, int row) {
        JLabel label = new JLabel(labelText);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        frame1.add(label, gbc);

        gbc.gridx = 1;
        frame1.add(field, gbc);
    }

    private void updateDays() {
        int month = monthComboBox.getSelectedIndex();
        int year = (int) yearComboBox.getSelectedItem();

        dayComboBox.removeAllItems();

        int daysInMonth;
        switch (month) {
            case 1:
                daysInMonth = isLeapYear(year) ? 29 : 28;
                break;
            case 0:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                daysInMonth = 31;
                break;
            default:
                daysInMonth = 30;
        }

        for (int day = 1; day <= daysInMonth; day++) {
            dayComboBox.addItem(day);
        }
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private boolean validateFields() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();
        String phone = phoneField.getText();
        int day = (int) dayComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (int) yearComboBox.getSelectedItem();
        Calendar dob = Calendar.getInstance();
        dob.set(year, month - 1, day);

        Calendar today = Calendar.getInstance();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.after(today)) {
            JOptionPane.showMessageDialog(frame1, "All fields are required and Date of Birth must be before today.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!name.matches("[a-zA-Z\\s]+")) {
            JOptionPane.showMessageDialog(frame1, "Name can only contain letters and spaces.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (username.equals(password)) {
            JOptionPane.showMessageDialog(frame1, "Username and password cannot be the same.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (password.length() < 8 ||
                !Pattern.compile("[A-Z]").matcher(password).find() ||
                !Pattern.compile("[a-z]").matcher(password).find() ||
                !Pattern.compile("[0-9]").matcher(password).find() ||
                !Pattern.compile("[!@#$%^&*()-+]").matcher(password).find()) {
            JOptionPane.showMessageDialog(frame1, "Password must be at least 8 characters, with uppercase, lowercase, digit, and one of the symbols", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$").matcher(email).matches()) {
            JOptionPane.showMessageDialog(frame1, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(frame1, "Phone number must be 10 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void createUserAccount() {
        String name = nameField.getText();
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText();
        int day = (int) dayComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        int year = (int) yearComboBox.getSelectedItem();
        Calendar dob = Calendar.getInstance();
        dob.set(year, month - 1, day);
        String role = (String) roleComboBox.getSelectedItem();
        String phone = phoneField.getText();
        java.sql.Date today = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
        java.sql.Date birthdate = new java.sql.Date(dob.getTimeInMillis());

        String sql = "INSERT INTO users (name, username, password, email, phone, dob, role, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for the query
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            stmt.setDate(6, birthdate);
            stmt.setString(7, role);
            stmt.setDate(8, today);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(frame1, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                frame1.dispose();
            } else {
                JOptionPane.showMessageDialog(frame1, "Error creating account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(frame1, "Username or email already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame1, "Error connecting to the database. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
