import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/*
    1. javac -cp .:mysql-connector-j-9.0.0.jar Test.java
    2. java -cp .:mysql-connector-j-9.0.0.jar Test
*/
public class Test {
    public static void main(String[] args) {
        // Replace these with your database credentials
        String url = "jdbc:mysql://localhost:3306";
        String username = "Temp";
        String password = "123456";

        Connection connection = null;

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish the connection
            connection = DriverManager.getConnection(url, username, password);
            if (connection != null) {
                System.out.println("Connected to the MySQL database successfully!");
                
                // Create a statement to execute the query
                Statement statement = connection.createStatement();
                
                // Execute the query to get the list of databases
                ResultSet resultSet = statement.executeQuery("SHOW DATABASES");
                
                System.out.println("List of databases:");
                while (resultSet.next()) {
                    // Print each database name
                    String dbName = resultSet.getString(1);
                    System.out.println(dbName);
                }
                
                // Close the statement and result set
                resultSet.close();
                statement.close();
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the MySQL database.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } finally {
            // Close the connection
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}

