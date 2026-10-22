package roadsafe.user;


public class Admin extends User implements Reportable, Notifiable {

    public Admin(int id, String username, String fullName) {
        super(id, username, fullName);
    }

    public String getRole() {
        return ADMIN;
    }

    public boolean canReport() {
        return true;
    }

    public boolean canUpdateStatus() {
        return true;
    }

    public boolean canAssignWorkers() {
        return true;
    }

    public boolean canDelete() {
        return true;
    }

    
    public boolean canSubmitReport() {
        return true;
    }

    
    public void receiveNotification(String message) {
        javax.swing.JOptionPane.showMessageDialog(null,
                message, "Notification for Admin",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
