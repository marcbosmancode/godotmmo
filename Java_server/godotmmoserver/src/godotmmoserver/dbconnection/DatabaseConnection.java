package godotmmoserver.dbconnection;

import godotmmoserver.constants.ServerConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author marcb
 */
public class DatabaseConnection {
    public Connection getConnection(){
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database");
        }
        return connection;
    }
}
