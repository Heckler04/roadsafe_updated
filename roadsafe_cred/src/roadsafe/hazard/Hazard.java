package roadsafe.hazard;


public class Hazard {

    
    public static final String[] TYPES = {
        "Pothole", "Damaged Road", "Obstacle",
        "Broken Streetlight", "Waterlogging", "Other"
    };
    public static final String[] SEVERITIES = {
        "Low", "Medium", "High", "Critical"
    };
    public static final String[] STATUSES = {
        "Reported", "In Progress", "Resolved"
    };

    private final int hazardId;
    private final String citizenName;
    private final String reportedBy;      
    private final String hazardType;
    private final String location;
    private final String description;
    private final String severity;
    private String status;                
    private String assignedWorker;        

    public Hazard(int hazardId, String citizenName, String reportedBy,
                  String hazardType, String location,
                  String description, String severity,
                  String status, String assignedWorker) {
        this.hazardId = hazardId;
        this.citizenName = citizenName;
        this.reportedBy = reportedBy;
        this.hazardType = hazardType;
        this.location = location;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.assignedWorker = assignedWorker;
    }

    
    public static boolean isValidStatus(String status) {
        for (int i = 0; i < STATUSES.length; i++) {
            if (STATUSES[i].equals(status)) {
                return true;
            }
        }
        return false;
    }

    public int getHazardId() { return hazardId; }
    public String getCitizenName() { return citizenName; }
    public String getReportedBy() { return reportedBy; }
    public String getHazardType() { return hazardType; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getAssignedWorker() { return assignedWorker; }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void setAssignedWorker(String workerUsername) {
        this.assignedWorker = workerUsername;
    }
}
