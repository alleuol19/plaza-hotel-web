import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnectionTest {

    // These MUST be static so the static testConnection method can use them
    // Get these from the "Variables" tab in Screenshot 2026-05-02 161858.png
	static String host = "switchyard.proxy.rlwy.net";     
    static String port = "27217";     
    static String dbName = "railway"; 
    static String dbUser = "root";     
    static String dbPass = "HUMUeVXnKdlhsHllvEQqxWZKErmocAhV";

    // The full URL for your live cloud database
    static String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

    public static void main(String[] args) {
        testConnection();
    }

    public static void testConnection() {
        System.out.println("Attempting to connect to Railway Cloud...");
        
        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass)) {
            System.out.println("✅ Connected to Railway MySQL successfully!");

            DatabaseMetaData metaData = conn.getMetaData();

            // Showing tables specifically in your Railway database
            System.out.println("\nChecking for tables in: " + dbName);
            String[] types = {"TABLE"}; 
            try (ResultSet tables = metaData.getTables(dbName, null, "%", types)) {
                boolean hasTables = false;
                while (tables.next()) {
                    hasTables = true;
                    String tableName = tables.getString("TABLE_NAME"); 
                    System.out.println("- Found Table: " + tableName);
                }
                if (!hasTables) {
                    System.out.println("⚠️ No tables found. Did you import your SQL from XAMPP yet?");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Connection failed! Check your Railway variables and internet.");
            e.printStackTrace();
        }
    }
}