package roadsafe.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import roadsafe.database.DatabaseManager;
import roadsafe.RoadSafeException;
import roadsafe.util.Validator;


public class RegisterDialog extends JDialog implements ActionListener {

    private final JTextField nameField = new JTextField(15);
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JPasswordField confirmField = new JPasswordField(15);
    private final JButton createButton = new JButton("Create Account");
    private final JButton cancelButton = new JButton("Cancel");

    public RegisterDialog(Frame owner) {
        super(owner, "Register as Citizen", true);  

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        fieldsPanel.add(new JLabel("Full Name:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("Username:"));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(new JLabel("Password:"));
        fieldsPanel.add(passwordField);
        fieldsPanel.add(new JLabel("Confirm Password:"));
        fieldsPanel.add(confirmField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        createButton.addActionListener(this);
        cancelButton.addActionListener(this);
        getRootPane().setDefaultButton(createButton);

        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createButton) {
            register();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }

    private void register() {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        
        String error = Validator.fullName(name);
        if (error == null) {
            error = Validator.username(username);
        }
        if (error == null) {
            error = Validator.password(password);
        }
        if (error == null && !password.equals(confirm)) {
            error = "Passwords do not match.";
        }
        if (error != null) {
            JOptionPane.showMessageDialog(this, error,
                    "Check your details", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DatabaseManager.getInstance()
                    .registerCitizen(username, name, password);
            JOptionPane.showMessageDialog(this,
                    "Account created! You can now log in as '"
                    + username + "'.");
            dispose();
        } catch (RoadSafeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
