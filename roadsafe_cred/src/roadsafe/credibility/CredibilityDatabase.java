package roadsafe.credibility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;




public class CredibilityDatabase {

    
    private static final String DB_URL = "jdbc:sqlite:roadsafe.db";

    private Connection connection;

    public CredibilityDatabase() {
        
    }

    private Connection connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    private void closeQuietly(ResultSet resultSet, Statement statement) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            
        }
    }

    
    public void initializeTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS credibility_ratings ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "hazard_id INTEGER NOT NULL, "
                + "rated_by TEXT NOT NULL, "
                + "genuine INTEGER NOT NULL, "
                + "UNIQUE(hazard_id, rated_by))";

        Statement statement = null;
        try {
            statement = connect().createStatement();
            statement.executeUpdate(sql);
        } finally {
            closeQuietly(null, statement);
        }
    }

    public void addRating(CredibilityRating rating) throws SQLException {
        String sql = "INSERT INTO credibility_ratings "
                + "(hazard_id, rated_by, genuine) VALUES (?, ?, ?)";

        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, rating.getHazardId());
            statement.setString(2, rating.getRatedBy());
            statement.setInt(3, rating.isGenuine() ? 1 : 0);
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }

    
    
    public boolean deleteRating(int hazardId, String username)
            throws SQLException {
        String sql = "DELETE FROM credibility_ratings "
                + "WHERE hazard_id = ? AND rated_by = ?";

        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, hazardId);
            statement.setString(2, username);
            return statement.executeUpdate() > 0;
        } finally {
            closeQuietly(null, statement);
        }
    }

    
    public void deleteRatingsForHazard(int hazardId) throws SQLException {
        String sql = "DELETE FROM credibility_ratings WHERE hazard_id = ?";

        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, hazardId);
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }

    public Vector getRatingsForHazard(int hazardId) throws SQLException {
        Vector ratings = new Vector();
        String sql = "SELECT id, hazard_id, rated_by, genuine "
                + "FROM credibility_ratings WHERE hazard_id = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, hazardId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                boolean genuine = resultSet.getInt("genuine") == 1;
                ratings.add(new CredibilityRating(
                        resultSet.getInt("id"),
                        resultSet.getInt("hazard_id"),
                        resultSet.getString("rated_by"),
                        genuine));
            }
        } finally {
            closeQuietly(resultSet, statement);
        }
        return ratings;
    }

    public boolean hasRated(int hazardId, String username)
            throws SQLException {
        String sql = "SELECT 1 FROM credibility_ratings "
                + "WHERE hazard_id = ? AND rated_by = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, hazardId);
            statement.setString(2, username);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            closeQuietly(resultSet, statement);
        }
    }

    public int countGenuine(int hazardId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM credibility_ratings "
                + "WHERE hazard_id = ? AND genuine = 1";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, hazardId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } finally {
            closeQuietly(resultSet, statement);
        }
    }

    public int countTotal(int hazardId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM credibility_ratings "
                + "WHERE hazard_id = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, hazardId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } finally {
            closeQuietly(resultSet, statement);
        }
    }
}
