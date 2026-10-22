package roadsafe.hazard;

import java.sql.SQLException;
import java.util.Vector;




public interface HazardRepository {
    void addHazard(Hazard hazard) throws SQLException;
    Vector getAllHazards() throws SQLException;
    Vector getHazardsByReporter(String username) throws SQLException;
    Vector getHazardsByWorker(String username) throws SQLException;
    void updateStatus(int id, String status) throws SQLException;
    void assignWorker(int id, String workerUsername) throws SQLException;
    void deleteHazard(int id) throws SQLException;
}
