package godotmmoserver.constants;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author marcb
 */
public class ServerConstants {
    public static final String NAME = "Godot engine mmo server";
    public static final String VERSION = "0.0.1";
    
    public static final int PORT = 8082;
    
    public static final int MAX_PLAYERS = 100;
    
    public static String DB_URL = "";
    public static String DB_USER = "";
    public static String DB_PASS = "";
    
    //load database info from a properties file
    static {
        try(InputStream input = new FileInputStream("src/godotmmoserver/constants/config.properties")) {
            Properties pr = new Properties();
            pr.load(input);
            
            DB_URL = pr.getProperty("DB_URL");
            DB_USER = pr.getProperty("DB_USER");
            DB_PASS = pr.getProperty("DB_PASS");
        } catch (Exception e) {
            System.out.println("Couldn't load the config.properties file");
        }
    }
}
