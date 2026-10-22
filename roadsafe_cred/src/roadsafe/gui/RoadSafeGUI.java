package roadsafe.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import roadsafe.database.DatabaseManager;
import roadsafe.RoadSafeException;
import roadsafe.credibility.CredibilityManager;
import roadsafe.hazard.Hazard;
import roadsafe.hazard.HazardManager;
import roadsafe.user.User;
import roadsafe.util.Validator;



public class RoadSafeGUI extends JFrame implements ActionListener {

    private static final String ALL = "All";

    private static final String[] COLUMN_NAMES = {
        "ID", "Citizen", "Type", "Location",
        "Description", "Severity", "Status", "Worker", "Score"
    };

    private final User user;
    private final HazardManager manager = new HazardManager();
    private final CredibilityManager credibility = new CredibilityManager();

    
    private Vector allHazards = new Vector();

    
    private final JTextField nameField = new JTextField();
    private final JTextField locationField = new JTextField();
    private final JTextField descriptionField = new JTextField();
    private final JComboBox typeBox = new JComboBox(Hazard.TYPES);
    private final JComboBox severityBox =
            new JComboBox(Hazard.SEVERITIES);

    
    private final JTextField searchField = new JTextField(18);
    private final JComboBox filterStatus =
            new JComboBox(withAll(Hazard.STATUSES));
    private final JComboBox filterSeverity =
            new JComboBox(withAll(Hazard.SEVERITIES));
    private final JComboBox filterType =
            new JComboBox(withAll(Hazard.TYPES));
    
    private static final String VIEW_CREDIBLE = "Credible / Not rated";
    private static final String VIEW_FLAGGED = "Flagged for review";
    private static final String VIEW_ALL = "All reports";
    private final JComboBox filterCredibility = new JComboBox(
            new String[] { VIEW_CREDIBLE, VIEW_FLAGGED, VIEW_ALL });
    private final JComboBox minScoreBox = new JComboBox(
            new String[] { "25", "50", "75" });
    private final JButton searchButton = new JButton("Search");
    private final JButton clearButton = new JButton("Clear");

    
    private final JComboBox statusBox =
            new JComboBox(Hazard.STATUSES);
    private final JComboBox workerBox = new JComboBox();
    private final JButton reportButton = new JButton("Submit Report");
    private final JButton refreshButton = new JButton("Refresh Reports");
    private final JButton updateButton = new JButton("Update Status");
    private final JButton assignButton = new JButton("Assign Worker");
    private final JButton addWorkerButton = new JButton("Add Worker");
    private final JButton deleteButton = new JButton("Delete Report");
    private final JButton rateGenuineButton = new JButton("Rate Genuine");
    private final JButton rateNotGenuineButton = new JButton("Rate Not Genuine");
    private final JButton withdrawButton = new JButton("Withdraw My Rating");
    private final JButton logoutButton = new JButton("Logout");
    private final JLabel countLabel = new JLabel(" ");

    private final HazardTableModel tableModel = new HazardTableModel(COLUMN_NAMES);
    private final JTable table = new JTable(tableModel);

    
    private class HazardTableModel extends DefaultTableModel {
        public HazardTableModel(String[] columnNames) {
            super(columnNames, 0);
        }

        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    public RoadSafeGUI(User user) {
        this.user = user;

        setTitle("RoadSafe - " + user.getRole() + " Dashboard");
        setSize(1200, 660);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(4).setPreferredWidth(250);

        
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("  Logged in as " + user.getFullName()
                + "  |  Role: " + user.getRole());
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        northPanel.add(headerPanel);

        if (user.canReport()) {
            JPanel formPanel = buildReportForm();
            formPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            northPanel.add(formPanel);
        }

        JPanel filterPanel = buildFilterPanel();
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        northPanel.add(filterPanel);

        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buildActionPanel(), BorderLayout.CENTER);
        countLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 0));
        southPanel.add(countLabel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        addListeners();
        loadWorkers();
        try {
            credibility.initialize();
        } catch (RoadSafeException ex) {
            showError(ex);
        }
        reloadFromDatabase();
    }

    private static String[] withAll(String[] items) {
        String[] result = new String[items.length + 1];
        result[0] = ALL;
        for (int i = 0; i < items.length; i++) {
            result[i + 1] = items[i];
        }
        return result;
    }

    private JPanel buildReportForm() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Report a Road Hazard"));

        
        if (user.getRole().equals(User.CITIZEN)) {
            nameField.setText(user.getFullName());
            nameField.setEditable(false);
        }

        form.add(new JLabel("Citizen Name:"));
        form.add(nameField);
        form.add(new JLabel("Hazard Type:"));
        form.add(typeBox);
        form.add(new JLabel("Location:"));
        form.add(locationField);
        form.add(new JLabel("Description:"));
        form.add(descriptionField);
        form.add(new JLabel("Severity:"));
        form.add(severityBox);
        return form;
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));

        panel.add(new JLabel("Search:"));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(new JLabel("Status:"));
        panel.add(filterStatus);
        panel.add(new JLabel("Severity:"));
        panel.add(filterSeverity);
        panel.add(new JLabel("Type:"));
        panel.add(filterType);
        panel.add(new JLabel("Show:"));
        panel.add(filterCredibility);
        panel.add(new JLabel("Min score %:"));
        panel.add(minScoreBox);
        panel.add(clearButton);
        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        if (user.canReport()) {
            panel.add(reportButton);
        }
        panel.add(refreshButton);

        
        panel.add(rateGenuineButton);
        panel.add(rateNotGenuineButton);
        panel.add(withdrawButton);

        if (user.canUpdateStatus()) {
            panel.add(new JLabel("Set status:"));
            panel.add(statusBox);
            panel.add(updateButton);
        }
        if (user.canAssignWorkers()) {
            panel.add(new JLabel("Worker:"));
            panel.add(workerBox);
            panel.add(assignButton);
            panel.add(addWorkerButton);
        }
        if (user.canDelete()) {
            panel.add(deleteButton);
        }
        return panel;
    }

    private void addListeners() {
        reportButton.addActionListener(this);
        refreshButton.addActionListener(this);
        updateButton.addActionListener(this);
        assignButton.addActionListener(this);
        addWorkerButton.addActionListener(this);
        deleteButton.addActionListener(this);
        rateGenuineButton.addActionListener(this);
        rateNotGenuineButton.addActionListener(this);
        withdrawButton.addActionListener(this);
        logoutButton.addActionListener(this);
        searchButton.addActionListener(this);
        clearButton.addActionListener(this);
        searchField.addActionListener(this);
        filterStatus.addActionListener(this);
        filterSeverity.addActionListener(this);
        filterType.addActionListener(this);
        filterCredibility.addActionListener(this);
        minScoreBox.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == reportButton) {
            reportHazard();
        } else if (source == refreshButton) {
            loadWorkers();
            reloadFromDatabase();
        } else if (source == updateButton) {
            updateStatus();
        } else if (source == assignButton) {
            assignWorker();
        } else if (source == addWorkerButton) {
            addWorker();
        } else if (source == deleteButton) {
            deleteReport();
        } else if (source == rateGenuineButton) {
            rateHazard(true);
        } else if (source == rateNotGenuineButton) {
            rateHazard(false);
        } else if (source == withdrawButton) {
            withdrawRating();
        } else if (source == logoutButton) {
            logout();
        } else if (source == clearButton) {
            clearFilters();
        } else if (source == searchButton || source == searchField
                || source == filterStatus || source == filterSeverity
                || source == filterType || source == filterCredibility
                || source == minScoreBox) {
            showMatchingHazards();
        }
    }

    private void loadWorkers() {
        if (!user.canAssignWorkers()) {
            return;
        }
        try {
            workerBox.removeAllItems();
            Vector workers = DatabaseManager.getInstance().getWorkers();
            for (int i = 0; i < workers.size(); i++) {
                workerBox.addItem(workers.get(i));
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void reloadFromDatabase() {
        try {
            allHazards = manager.getVisibleHazards(user);
            showMatchingHazards();
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    
    private double getMinScore() {
        try {
            return Double.parseDouble((String) minScoreBox.getSelectedItem());
        } catch (NumberFormatException ex) {
            return CredibilityManager.DEFAULT_MIN_SCORE;
        }
    }

    
    private void showMatchingHazards() {
        String text = searchField.getText().trim().toLowerCase();
        String status = (String) filterStatus.getSelectedItem();
        String severity = (String) filterSeverity.getSelectedItem();
        String type = (String) filterType.getSelectedItem();
        String view = (String) filterCredibility.getSelectedItem();
        double minScore = getMinScore();

        
        int selectedId = -1;
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            selectedId = ((Integer) tableModel.getValueAt(selectedRow, 0))
                    .intValue();
        }

        tableModel.setRowCount(0);
        int flagged = 0;
        for (int i = 0; i < allHazards.size(); i++) {
            Hazard hazard = (Hazard) allHazards.get(i);
            if (!matchesFilters(hazard, text, status, severity, type)) {
                continue;
            }

            String scoreText = "?";
            boolean isFlagged = false;
            try {
                isFlagged = credibility.isFlagged(hazard.getHazardId(),
                        minScore);
                scoreText = credibility.getScoreText(hazard.getHazardId(),
                        minScore);
            } catch (RoadSafeException ex) {
                
            }
            if (isFlagged) {
                flagged++;
            }

            
            if (view.equals(VIEW_CREDIBLE) && isFlagged) {
                continue;
            }
            if (view.equals(VIEW_FLAGGED) && !isFlagged) {
                continue;
            }

            Object[] row = {
                hazard.getHazardId(),
                hazard.getCitizenName(),
                hazard.getHazardType(),
                hazard.getLocation(),
                hazard.getDescription(),
                hazard.getSeverity(),
                hazard.getStatus(),
                hazard.getAssignedWorker(),
                scoreText
            };
            tableModel.addRow(row);
            if (hazard.getHazardId() == selectedId) {
                int newRow = tableModel.getRowCount() - 1;
                table.setRowSelectionInterval(newRow, newRow);
            }
        }
        countLabel.setText("Showing " + tableModel.getRowCount() + " of "
                + allHazards.size() + " reports"
                + (flagged > 0 ? "  |  " + flagged
                        + " flagged for review (score below "
                        + (int) minScore + "%)" : ""));
    }

    private boolean matchesFilters(Hazard hazard, String text, String status,
                                   String severity, String type) {
        if (!status.equals(ALL) && !status.equals(hazard.getStatus())) {
            return false;
        }
        if (!severity.equals(ALL) && !severity.equals(hazard.getSeverity())) {
            return false;
        }
        if (!type.equals(ALL) && !type.equals(hazard.getHazardType())) {
            return false;
        }
        if (text.isEmpty()) {
            return true;
        }

        
        String everything = hazard.getHazardId() + " "
                + hazard.getCitizenName() + " "
                + hazard.getHazardType() + " "
                + hazard.getLocation() + " "
                + hazard.getDescription() + " "
                + hazard.getSeverity() + " "
                + hazard.getStatus() + " "
                + hazard.getAssignedWorker();
        return everything.toLowerCase().contains(text);
    }

    private void clearFilters() {
        searchField.setText("");
        filterStatus.setSelectedIndex(0);
        filterSeverity.setSelectedIndex(0);
        filterType.setSelectedIndex(0);
        filterCredibility.setSelectedIndex(0);
        minScoreBox.setSelectedItem("50");
        showMatchingHazards();
    }

    private int getSelectedHazardId() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Select a hazard from the table first.");
            return -1;
        }
        Object value = tableModel.getValueAt(row, 0);
        return ((Integer) value).intValue();
    }

    private void reportHazard() {
        if (!user.canReport()) {
            JOptionPane.showMessageDialog(this,
                    "You are not allowed to submit reports.");
            return;
        }

        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        String description = descriptionField.getText().trim();

        String error = Validator.fullName(name);
        if (error == null) {
            error = Validator.location(location);
        }
        if (error == null) {
            error = Validator.description(description);
        }
        if (error != null) {
            JOptionPane.showMessageDialog(this, error,
                    "Check the form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Hazard hazard = new Hazard(0, name, user.getUsername(),
                    (String) typeBox.getSelectedItem(),
                    location, description,
                    (String) severityBox.getSelectedItem(),
                    "Reported", "Unassigned");
            manager.reportHazard(user, hazard);
            
            locationField.setText("");
            descriptionField.setText("");
            reloadFromDatabase();
            JOptionPane.showMessageDialog(this, "Hazard reported successfully.");
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    private void updateStatus() {
        if (!user.canUpdateStatus()) {
            JOptionPane.showMessageDialog(this,
                    "You are not allowed to update status.");
            return;
        }

        int id = getSelectedHazardId();
        if (id == -1) {
            return;
        }

        try {
            String newStatus = (String) statusBox.getSelectedItem();
            manager.updateStatus(user, id, newStatus);
            reloadFromDatabase();
            JOptionPane.showMessageDialog(this, "Status updated successfully.");
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    private void assignWorker() {
        if (!user.canAssignWorkers()) {
            JOptionPane.showMessageDialog(this,
                    "Only an Admin can assign workers.");
            return;
        }

        int id = getSelectedHazardId();
        if (id == -1) {
            return;
        }

        User worker = (User) workerBox.getSelectedItem();
        if (worker == null) {
            JOptionPane.showMessageDialog(this,
                    "There are no maintenance workers to assign.");
            return;
        }

        try {
            manager.assignWorker(user, id, worker.getUsername());
            reloadFromDatabase();
            JOptionPane.showMessageDialog(this,
                    "Assigned to " + worker.getFullName() + ".");
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    private void addWorker() {
        if (!user.canAssignWorkers()) {
            return;
        }
        AddWorkerDialog dialog = new AddWorkerDialog(this);
        dialog.setVisible(true);
        
        loadWorkers();
    }

    private void deleteReport() {
        if (!user.canDelete()) {
            return;
        }

        int id = getSelectedHazardId();
        if (id == -1) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete report #" + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manager.deleteHazard(user, id);
            credibility.deleteRatings(id);
            reloadFromDatabase();
            JOptionPane.showMessageDialog(this, "Report deleted successfully.");
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    
    private void rateHazard(boolean genuine) {
        int id = getSelectedHazardId();
        if (id == -1) {
            return;
        }

        Hazard selected = findHazard(id);
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "That report no longer exists. Refresh the list.");
            return;
        }

        try {
            credibility.rateHazard(user, selected, genuine);
            reloadFromDatabase();
            String msg = (genuine ? "Marked as Genuine." : "Marked as Not Genuine.")
                    + "\nCredibility of report #" + id + ": "
                    + credibility.getScoreText(id, getMinScore());
            JOptionPane.showMessageDialog(this, msg);
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    
    private void withdrawRating() {
        int id = getSelectedHazardId();
        if (id == -1) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this,
                "Withdraw your rating of report #" + id + "?",
                "Withdraw Rating", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            credibility.withdrawRating(user, id);
            reloadFromDatabase();
            JOptionPane.showMessageDialog(this,
                    "Your rating was withdrawn.\nCredibility of report #"
                    + id + ": " + credibility.getScoreText(id, getMinScore()));
        } catch (RoadSafeException ex) {
            showError(ex);
        }
    }

    
    private Hazard findHazard(int id) {
        for (int i = 0; i < allHazards.size(); i++) {
            Hazard h = (Hazard) allHazards.get(i);
            if (h.getHazardId() == id) {
                return h;
            }
        }
        return null;
    }

    private void logout() {
        dispose();
        LoginGUI loginWindow = new LoginGUI();
        loginWindow.setVisible(true);
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "RoadSafe Error", JOptionPane.ERROR_MESSAGE);
    }
}
