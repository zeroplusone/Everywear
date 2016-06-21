package maclab.everywear;
import java.sql.*;

public class EverywearDB {
    // The JDBC Connector Class.
    private static final String dbClassName = "org.mariadb.jdbc.Driver";
    // private static final String dbClassName = "com.mysql.jdbc.Driver";

    // Database
    private final String CON_STR="jdbc:mariadb://" + DBInfo.HOST +":"+ DBInfo.PORT +"/"+ DBInfo.DATABASE;
    private Connection dbCon;
    private Statement stmt = null;
    private ResultSet rs = null;

    public static void main(String[] args) {
        new EverywearDB();
    }

    EverywearDB() {
        try {
            // Class.forName(xxx) loads the jdbc classes and
            // creates a drivermanager class factory
            // Class.forName(dbClassName);

            System.out.println(CON_STR);
            dbCon = DriverManager.getConnection(CON_STR, DBInfo.USER, DBInfo.PASSWORD);
            System.out.println("connect to database!");

            stmt = dbCon.createStatement();
            createTableIfNotExist();

        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(e);
        }
        finally {
            // it is a good idea to release
            // resources in a finally{} block
            // in reverse-order of their creation
            // if they are no-longer needed
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore
                rs = null;
            }
            /*
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore
                stmt = null;
            }
            if (dbCon != null) {
                try {
                    dbCon.close();
                    System.out.println("Close database connection.");
                } catch (SQLException e) {} // ignore
            }
            */
        }
    }

    private void createTableIfNotExist() throws SQLException {
        final String USER_TABLE = "User";
        System.out.println("Create table if not exists: " + USER_TABLE);
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + USER_TABLE + "("
            + "id VARCHAR(128) PRIMARY KEY,"
            + "createdtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "pic VARCHAR(256) NOT NULL,"
            + "name VARCHAR(64) NOT NULL"
            + ") CHARACTER SET = UTF8";
        stmt.execute(sqlCreate);

        final String POST_TABLE = "Post";
        System.out.println("Create table if not exists: " + POST_TABLE);
        sqlCreate = "CREATE TABLE IF NOT EXISTS " + POST_TABLE + "("
            + "no int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
            + "user_account VARCHAR(64),"
            + "createdtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "updatedtime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "ori_pic VARCHAR(256),"
            + "weather_pic VARCHAR(256),"
            + "city_zh VARCHAR(40),"
            + "city_en VARCHAR(32),"
            + "logo_position VARCHAR(32),"
            + "FOREIGN KEY (user_account) REFERENCES User (id) "
            + "ON DELETE CASCADE"
            + ") CHARACTER SET = UTF8";
        stmt.execute(sqlCreate);

        final String SCORE_TABLE = "Score";
        System.out.println("Create table if not exists: " + SCORE_TABLE);
        sqlCreate = "CREATE TABLE IF NOT EXISTS " + SCORE_TABLE + "("
            + "post_index int NOT NULL,"
            + "user_account VARCHAR(64),"
            + "score int NOT NULL DEFAULT 0,"
            + "UNIQUE KEY `id` (`user_account`, `post_index`)"
            + ") CHARACTER SET = UTF8";
        stmt.execute(sqlCreate);
    }
}
