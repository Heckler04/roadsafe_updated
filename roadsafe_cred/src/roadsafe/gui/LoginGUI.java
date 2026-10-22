package roadsafe.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import roadsafe.database.DatabaseManager;
import roadsafe.user.User;


public class LoginGUI extends JFrame implements ActionListener {

    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton loginButton = new JButton("Login");
    private final JButton registerButton = new JButton("Register (Citizen)");

    public LoginGUI() {
        setTitle("RoadSafe - Login");
        setSize(420, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JLabel heading = new JLabel("RoadSafe", SwingConstants.CENTER);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 22f));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 5, 25));
        fieldsPanel.add(new JLabel("Username:"));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(new JLabel("Password:"));
        fieldsPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        
        loginButton.addActionListener(this);
        registerButton.addActionListener(this);
        passwordField.addActionListener(this);   
        usernameField.addActionListener(this);

        add(heading, BorderLayout.NORTH);
        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == loginButton || source == passwordField) {
            login();
        } else if (source == usernameField) {
            passwordField.requestFocusInWindow();
        } else if (source == registerButton) {
            RegisterDialog dialog = new RegisterDialog(this);
            dialog.setVisible(true);
        }
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Enter both username and password.");
            return;
        }

        try {
            User user = DatabaseManager.getInstance()
                    .authenticate(username, password);

            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Incorrect username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocusInWindow();
                return;
            }

            dispose();
            RoadSafeGUI dashboard = new RoadSafeGUI(user);
            dashboard.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "RoadSafe Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
