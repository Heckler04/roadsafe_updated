package roadsafe.user;


public class MaintenanceWorker extends User implements Notifiable {

    public MaintenanceWorker(int id, String username, String fullName) {
        super(id, username, fullName);
    }

    public String getRole() {
        return WORKER;
    }

    public boolean canUpdateStatus() {
        return true;
    }

    
    public void receiveNotification(String message) {
        javax.swing.JOptionPane.showMessageDialog(null,
                message, "Notification for " + getFullName(),
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
