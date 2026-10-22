package roadsafe.database;



class DbConfig {

    static final String DB_FILE = "roadsafe.db";
    static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DbConfig() {
        
    }
}
