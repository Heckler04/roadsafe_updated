package roadsafe;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import roadsafe.database.DatabaseManager;
import roadsafe.gui.LoginGUI;


public class Main {

    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            
        }

        
        try {
            DatabaseManager.getInstance().initializeDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not open the SQLite database:\n" + e.getMessage()
                    + "\n\nCheck that lib/sqlite-jdbc.jar is on the build path "
                    + "and that the folder is writable.",
                    "RoadSafe - Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        
        LoginGUI loginWindow = new LoginGUI();
        loginWindow.setVisible(true);
    }
}
