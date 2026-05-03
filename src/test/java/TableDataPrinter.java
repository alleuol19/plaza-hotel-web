import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class TableDataPrinter {

    // --- PASTE YOUR RAILWAY VARIABLES HERE ---
	static String host = "switchyard.proxy.rlwy.net";     
    static String port = "27217";     
    static String dbName = "railway"; 
    static String dbUser = "root";     
    static String dbPass = "HUMUeVXnKdlhsHllvEQqxWZKErmocAhV";

    // The live cloud URL
    static String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

   
        String tableName = "Users"; 



    public static void printTableData(String tableName) {
        String query = "SELECT * FROM " + tableName;

        System.out.println("Connecting to Railway to fetch data from: " + tableName + "...");

        try (Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("✅ Connected! Printing results:\n");

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount(); 

            // Print Header
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i).toUpperCase() + "\t\t");
            }
            System.out.println("\n---------------------------------------------------------");
            
            // Print Rows
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    System.out.print(value + "\t\t");
                }
                System.out.println(); 
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving data from table: " + tableName);
            System.err.println("Make sure your table exists in the Railway database.");
            e.printStackTrace();
        }
    }
}