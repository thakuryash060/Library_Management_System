package library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class user_class {
    static String Username;
    private JFrame f;
    private JLabel welcomeLabel, usernameLabel, passwordLabel;
    private JTextField usernameField;
    protected JPasswordField passwordField;
    private JButton signInButton, forgetPasswordButton, signUpButton;

    user_class() {
        f = new JFrame("Library Management System - User Login");
        f.setSize(800, 600);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());

        background_panel bgPanel = new background_panel("C:\\Users\\thaku\\Downloads\\library-shelf.jpg");
        bgPanel.setLayout(null);

        welcomeLabel = new JLabel("WELCOME TO LIBRARY MANAGEMENT SYSTEM");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLACK);
        welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
        welcomeLabel.setBounds(0, 25, 800, 50);

        usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(Color.BLACK);
        usernameLabel.setBounds(250, 100, 100, 30);

        passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordLabel.setForeground(Color.BLACK);
        passwordLabel.setBounds(250, 150, 100, 30);

        usernameField = new JTextField();
        usernameField.setBounds(350, 100, 200, 30);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));

        passwordField = new JPasswordField();
        passwordField.setBounds(350, 150, 200, 30);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));


        signInButton = createStyledButton("SIGN IN");
        signInButton.setBounds(250, 250, 130, 30);
        signInButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleSignIn();
            }
        });

        forgetPasswordButton = createStyledButton("Forget Password");
        forgetPasswordButton.setBounds(50, 500, 130, 30);
        forgetPasswordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new forget();
            }
        });

        signUpButton = createStyledButton("SIGN UP");
        signUpButton.setBounds(640, 500, 130, 30);
        signUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new sign_up();
            }
        });


        bgPanel.add(welcomeLabel);
        bgPanel.add(usernameLabel);
        bgPanel.add(usernameField);
        bgPanel.add(passwordLabel);
        bgPanel.add(passwordField);
        bgPanel.add(signInButton);
        bgPanel.add(forgetPasswordButton);
        bgPanel.add(signUpButton);

        f.add(bgPanel);
        f.setVisible(true);
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

    private void handleSignIn() {
        Username = usernameField.getText();
        String Password = new String(passwordField.getPassword());
        if (Username.isEmpty() || Password.isEmpty()) {
            JOptionPane.showMessageDialog(f, "Enter details.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "radicals133@")) {
                    String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                    PreparedStatement pst = con.prepareStatement(query);
                    pst.setString(1, Username);
                    pst.setString(2, Password);

                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        String role = rs.getString("role");
                        updateSignInTime(con, Username);

                        switch (role) {
                            case "Student":
                                JOptionPane.showMessageDialog(f, "Welcome to the " + role + " Dashboard!");
                                new CInterface(Username);
                                break;
                            case "Faculty":
                                JOptionPane.showMessageDialog(f, "Welcome to the " + role + " Dashboard!");
                                new CInterface(Username);
                                break;
                            case "Librarian":
                                JOptionPane.showMessageDialog(f, "Welcome to the Librarian Dashboard!");
                                new LibrarianInterface(Username);
                                break;
                            default:
                                JOptionPane.showMessageDialog(f, "Role not recognized.", "Error", JOptionPane.ERROR_MESSAGE);
                                break;
                        }

                        usernameField.setText("");
                        passwordField.setText("");
                        f.dispose();
                    } else {
                        JOptionPane.showMessageDialog(f, "No matching record found.", "Error", JOptionPane.ERROR_MESSAGE);
                        usernameField.setText("");
                        passwordField.setText("");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(f, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(f, "Database driver not found.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private static void updateSignInTime(Connection con, String username) throws SQLException {
        String updateQuery = "UPDATE users SET sign_in = CURRENT_TIMESTAMP WHERE username = ?";
        PreparedStatement updatePst = con.prepareStatement(updateQuery);
        updatePst.setString(1, username);
        updatePst.executeUpdate();
    }

    public static void main(String[] args) {
        new user_class();
    }
}
