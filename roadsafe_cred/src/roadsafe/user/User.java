package roadsafe.user;



public abstract class User {

    
    public static final String CITIZEN = "Citizen";
    public static final String WORKER = "Maintenance Worker";
    public static final String ADMIN = "Admin";

    private final int userId;
    private final String username;
    private final String fullName;

    
    protected User(int userId, String username, String fullName) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
    }

    
    public static User create(int id, String username,
                              String fullName, String role) {
        switch (role) {
            case ADMIN:
                return new Admin(id, username, fullName);
            case WORKER:
                return new MaintenanceWorker(id, username, fullName);
            default:
                return new Citizen(id, username, fullName);
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    
    public abstract String getRole();

    
    public boolean canReport() {
        return false;
    }

    public boolean canUpdateStatus() {
        return false;
    }

    public boolean canAssignWorkers() {
        return false;
    }

    public boolean canDelete() {
        return false;
    }

    public String toString() {
        return fullName + " (" + username + ")";
    }
}
