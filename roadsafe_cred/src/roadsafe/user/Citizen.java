package roadsafe.user;


public class Citizen extends User implements Reportable {

    public Citizen(int id, String username, String fullName) {
        super(id, username, fullName);
    }

    public String getRole() {
        return CITIZEN;
    }

    public boolean canReport() {
        return true;
    }

    
    public boolean canSubmitReport() {
        return true;
    }
}
