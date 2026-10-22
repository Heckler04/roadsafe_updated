package roadsafe.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import roadsafe.RoadSafeException;
import roadsafe.hazard.Hazard;
import roadsafe.hazard.HazardRepository;
import roadsafe.user.User;
import roadsafe.util.PasswordUtil;




public class DatabaseManager implements HazardRepository {

    private static final DatabaseManager instance = new DatabaseManager();
    private Connection connection;

    private DatabaseManager() {
        
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    private Connection connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DbConfig.DB_URL);
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

    public void initializeDatabase() throws SQLException {
        Connection con = connect();
        createTables(con);
        addDefaultUsers(con);
    }

    private void createTables(Connection con) throws SQLException {
        
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "full_name TEXT NOT NULL, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL)";

        String createHazards = "CREATE TABLE IF NOT EXISTS hazards ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "citizen_name TEXT NOT NULL, "
                + "reported_by TEXT, "
                + "hazard_type TEXT NOT NULL, "
                + "location TEXT NOT NULL, "
                + "description TEXT, "
                + "severity TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'Reported', "
                + "assigned_worker TEXT DEFAULT 'Unassigned')";

        Statement statement = null;
        try {
            statement = con.createStatement();
            statement.executeUpdate(createUsers);
            statement.executeUpdate(createHazards);
        } finally {
            closeQuietly(null, statement);
        }
    }

    private void addDefaultUsers(Connection con) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        int userCount = 0;
        try {
            statement = con.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) FROM users");
            if (resultSet.next()) {
                userCount = resultSet.getInt(1);
            }
        } finally {
            closeQuietly(resultSet, statement);
        }

        if (userCount > 0) {
            return;
        }

        insertUser(con, "admin", "System Admin", "admin123", User.ADMIN);
        insertUser(con, "worker1", "Ravi Kumar", "worker123", User.WORKER);
        insertUser(con, "worker2", "Anita Joseph", "worker123", User.WORKER);
        insertUser(con, "citizen", "Demo Citizen", "citizen123", User.CITIZEN);
    }

    private void insertUser(Connection con, String username, String fullName,
                            String password, String role)
            throws SQLException {
        String sql = "INSERT INTO users "
                + "(username, full_name, password, role) "
                + "VALUES (?, ?, ?, ?)";

        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, fullName);
            statement.setString(3, password);
            statement.setString(4, role);
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }

    
    public User authenticate(String username, String password)
            throws SQLException {
        String sql = "SELECT id, username, full_name, password, role "
                + "FROM users WHERE username = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            String stored = resultSet.getString("password");
            if (!PasswordUtil.matches(password, stored)) {
                return null;
            }
            return readUser(resultSet);
        } finally {
            closeQuietly(resultSet, statement);
        }
    }

    private User readUser(ResultSet resultSet) throws SQLException {
        return User.create(resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("full_name"),
                resultSet.getString("role"));
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setString(1, username);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } finally {
            closeQuietly(resultSet, statement);
        }
    }

    
    public void registerCitizen(String username, String fullName,
                                String password) throws RoadSafeException {
        try {
            if (usernameExists(username)) {
                throw new RoadSafeException("That username is already taken.");
            }
            insertUser(connect(), username, fullName, password, User.CITIZEN);
        } catch (SQLException e) {
            throw new RoadSafeException(
                    "Could not create the account: " + e.getMessage(), e);
        }
    }

    
    public void registerWorker(String username, String fullName,
                               String password) throws RoadSafeException {
        try {
            if (usernameExists(username)) {
                throw new RoadSafeException("That username is already taken.");
            }
            insertUser(connect(), username, fullName, password, User.WORKER);
        } catch (SQLException e) {
            throw new RoadSafeException(
                    "Could not create the worker: " + e.getMessage(), e);
        }
    }

    public Vector getWorkers() throws SQLException {
        Vector workers = new Vector();
        String sql = "SELECT id, username, full_name, role FROM users "
                + "WHERE role = ? ORDER BY full_name";

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setString(1, User.WORKER);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                workers.add(readUser(resultSet));
            }
        } finally {
            closeQuietly(resultSet, statement);
        }
        return workers;
    }

    

    public void addHazard(Hazard hazard) throws SQLException {
        String sql = "INSERT INTO hazards "
                + "(citizen_name, reported_by, hazard_type, location, "
                + "description, severity, status, assigned_worker) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setString(1, hazard.getCitizenName());
            statement.setString(2, hazard.getReportedBy());
            statement.setString(3, hazard.getHazardType());
            statement.setString(4, hazard.getLocation());
            statement.setString(5, hazard.getDescription());
            statement.setString(6, hazard.getSeverity());
            statement.setString(7, "Reported");
            statement.setString(8, "Unassigned");
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }

    public Vector getAllHazards() throws SQLException {
        return readHazards("SELECT * FROM hazards ORDER BY id DESC", null);
    }

    public Vector getHazardsByReporter(String username)
            throws SQLException {
        return readHazards("SELECT * FROM hazards WHERE reported_by = ? "
                + "ORDER BY id DESC", username);
    }

    public Vector getHazardsByWorker(String username)
            throws SQLException {
        return readHazards("SELECT * FROM hazards WHERE assigned_worker = ? "
                + "ORDER BY id DESC", username);
    }

    private Vector readHazards(String sql, String parameter)
            throws SQLException {
        Vector hazards = new Vector();

        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connect().prepareStatement(sql);
            if (parameter != null) {
                statement.setString(1, parameter);
            }
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                hazards.add(readHazard(resultSet));
            }
        } finally {
            closeQuietly(resultSet, statement);
        }
        return hazards;
    }

    private Hazard readHazard(ResultSet resultSet) throws SQLException {
        return new Hazard(
                resultSet.getInt("id"),
                resultSet.getString("citizen_name"),
                resultSet.getString("reported_by"),
                resultSet.getString("hazard_type"),
                resultSet.getString("location"),
                resultSet.getString("description"),
                resultSet.getString("severity"),
                resultSet.getString("status"),
                resultSet.getString("assigned_worker"));
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE hazards SET status = ? WHERE id = ?";
        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setString(1, status);
            statement.setInt(2, id);
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }

    public void assignWorker(int id, String workerUsername) throws SQLException {
        String sql = "UPDATE hazards SET assigned_worker = ? WHERE id = ?";
        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setString(1, workerUsername);
            statement.setInt(2, id);
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }

    public void deleteHazard(int id) throws SQLException {
        String sql = "DELETE FROM hazards WHERE id = ?";
        PreparedStatement statement = null;
        try {
            statement = connect().prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } finally {
            closeQuietly(null, statement);
        }
    }
}
