package DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB
{
    private static final String URL = "jdbc:mysql://localhost:3306/EMS";
    private static final String USERNAME = "Temp";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB.URL, DB.USERNAME, DB.PASSWORD);
    }
}