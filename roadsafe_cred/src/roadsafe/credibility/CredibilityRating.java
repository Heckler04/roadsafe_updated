package roadsafe.credibility;


public class CredibilityRating {

    private final int ratingId;
    private final int hazardId;
    private final String ratedBy;   
    private final boolean genuine;  

    public CredibilityRating(int ratingId, int hazardId,
                             String ratedBy, boolean genuine) {
        this.ratingId = ratingId;
        this.hazardId = hazardId;
        this.ratedBy = ratedBy;
        this.genuine = genuine;
    }

    public int getRatingId() {
        return ratingId;
    }

    public int getHazardId() {
        return hazardId;
    }

    public String getRatedBy() {
        return ratedBy;
    }

    public boolean isGenuine() {
        return genuine;
    }
}
