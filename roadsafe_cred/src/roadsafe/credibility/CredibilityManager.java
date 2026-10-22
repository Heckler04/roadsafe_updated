package roadsafe.credibility;

import java.sql.SQLException;
import java.util.Vector;
import roadsafe.RoadSafeException;
import roadsafe.hazard.Hazard;
import roadsafe.user.User;











public class CredibilityManager {

    
    public static final double DEFAULT_MIN_SCORE = 50.0;

    
    
    
    
    public static final int PRIOR_WEIGHT = 4;

    private final CredibilityDatabase database;

    
    public CredibilityManager() {
        this.database = new CredibilityDatabase();
    }

    
    public void initialize() throws RoadSafeException {
        try {
            database.initializeTable();
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    
    
    public void rateHazard(User user, Hazard hazard, boolean genuine)
            throws RoadSafeException {
        if (user == null) {
            throw new RoadSafeException("You must be logged in to rate.");
        }
        if (hazard == null || hazard.getHazardId() <= 0) {
            throw new RoadSafeException("Invalid hazard.");
        }
        if (user.getUsername().equals(hazard.getReportedBy())) {
            throw new RoadSafeException(
                    "You cannot rate a report that you submitted yourself.");
        }
        try {
            if (database.hasRated(hazard.getHazardId(), user.getUsername())) {
                throw new RoadSafeException(
                        "You have already rated this hazard.");
            }
            CredibilityRating rating = new CredibilityRating(
                    0, hazard.getHazardId(), user.getUsername(), genuine);
            database.addRating(rating);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    
    
    public void withdrawRating(User user, int hazardId)
            throws RoadSafeException {
        if (user == null) {
            throw new RoadSafeException("You must be logged in.");
        }
        if (hazardId <= 0) {
            throw new RoadSafeException("Invalid hazard.");
        }
        try {
            if (!database.deleteRating(hazardId, user.getUsername())) {
                throw new RoadSafeException(
                        "You have not rated this hazard, so there is "
                        + "nothing to withdraw.");
            }
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    
    public int getRatingCount(int hazardId) throws RoadSafeException {
        try {
            return database.countTotal(hazardId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    
    
    
    
    
    
    
    
    
    
    public double getCredibilityScore(int hazardId)
            throws RoadSafeException {
        try {
            int total = database.countTotal(hazardId);
            int genuine = database.countGenuine(hazardId);
            return ((genuine + PRIOR_WEIGHT / 2.0) * 100.0)
                    / (total + PRIOR_WEIGHT);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    
    public boolean isPending(int hazardId) throws RoadSafeException {
        return getRatingCount(hazardId) == 0;
    }

    
    public boolean isFlagged(int hazardId, double minScore)
            throws RoadSafeException {
        if (isPending(hazardId)) {
            return false;
        }
        return getCredibilityScore(hazardId) < minScore;
    }

    
    public String getScoreText(int hazardId, double minScore)
            throws RoadSafeException {
        int total = getRatingCount(hazardId);
        if (total == 0) {
            return "Not rated yet";
        }
        double score = getCredibilityScore(hazardId);
        int genuine = 0;
        try {
            genuine = database.countGenuine(hazardId);
        } catch (SQLException e) {
            throw wrap(e);
        }
        String text = String.format("%.0f%%", score)
                + " (" + genuine + " of " + total
                + (total == 1 ? " rating" : " ratings") + " genuine)";
        if (score < minScore) {
            text = text + " - FLAGGED";
        }
        return text;
    }

    
    
    public Vector getCredibleHazards(Vector allHazards, double minScore)
            throws RoadSafeException {
        Vector result = new Vector();
        if (allHazards == null) {
            return result;
        }
        for (int i = 0; i < allHazards.size(); i++) {
            Hazard h = (Hazard) allHazards.get(i);
            if (!isFlagged(h.getHazardId(), minScore)) {
                result.add(h);
            }
        }
        return result;
    }

    
    public Vector getCredibleHazards(Vector allHazards)
            throws RoadSafeException {
        return getCredibleHazards(allHazards, DEFAULT_MIN_SCORE);
    }

    
    public Vector getFlaggedHazards(Vector allHazards, double minScore)
            throws RoadSafeException {
        Vector result = new Vector();
        if (allHazards == null) {
            return result;
        }
        for (int i = 0; i < allHazards.size(); i++) {
            Hazard h = (Hazard) allHazards.get(i);
            if (isFlagged(h.getHazardId(), minScore)) {
                result.add(h);
            }
        }
        return result;
    }

    
    public Vector getFlaggedHazards(Vector allHazards)
            throws RoadSafeException {
        return getFlaggedHazards(allHazards, DEFAULT_MIN_SCORE);
    }

    
    public boolean hasUserRated(int hazardId, String username)
            throws RoadSafeException {
        try {
            return database.hasRated(hazardId, username);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    
    public void deleteRatings(int hazardId) throws RoadSafeException {
        try {
            database.deleteRatingsForHazard(hazardId);
        } catch (SQLException e) {
            throw wrap(e);
        }
    }

    private RoadSafeException wrap(SQLException e) {
        return new RoadSafeException(e.getMessage(), e);
    }
}
