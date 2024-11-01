package DBUtil;

import DBUtil.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.sql.*;
import javax.sql.rowset.RowSetWarning;


public class AssetDAO {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/EMS";
    private static final String USERNAME = "Temp";
    private static final String PASSWORD = "123456";

    // Method to establish a connection to the database
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static String createAsset(String organizationId, String name,String price,String purchaseDate,String status)
    {
        String getMaxIdSql = "SELECT MAX(AssetID) FROM Asset WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Asset (OrganizationID, AssetID, Name, Price, PurchaseDate, Status) Values(?, ?, ?, ?, ?, ?)";

        int orgId = Integer.parseInt(organizationId);

        try (Connection connection = DB.getConnection();
             PreparedStatement maxIdStmt = connection.prepareStatement(getMaxIdSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            
            // Step 1: Get the maximum EmployeeID for the organization
            maxIdStmt.setInt(1, orgId);
            ResultSet resultSet = maxIdStmt.executeQuery();
            int newAssetId = 1; // Default to 1 if no employees exist
            
            if (resultSet.next()) {
                String maxAssetIdStr = resultSet.getString(1);
                if (maxAssetIdStr != null) {
                    newAssetId = Integer.parseInt(maxAssetIdStr) + 1;
                }
            }
            
            // Step 2: Insert the new employee with the incremented EmployeeID
            
            insertStmt.setInt(1, orgId);
            insertStmt.setInt(2, newAssetId);
            insertStmt.setString(3,  name);
            insertStmt.setDouble(4, Double.parseDouble(price));
            insertStmt.setString(5, purchaseDate);
            insertStmt.setString(6, status);

            insertStmt.executeUpdate();
            return String.valueOf(newAssetId);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }   
    public static Map<String, Object> readAsset(String organizationId, String assetId) {
        String sql = "SELECT * FROM Asset WHERE OrganizationID = ? AND AssetID = ?";
        Map<String, Object> assetData = new HashMap<>();

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(assetId));
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                assetData.put("Name", resultSet.getString("Name"));
                assetData.put("Price", resultSet.getString("Price"));
                assetData.put("PurchaseDate", resultSet.getString("PurchaseDate"));
                assetData.put("Status", resultSet.getString("Status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assetData;
    }

        // Method to get all assets assigned to an employee
    public static Map<String, Map<String, Object>> readAssignedAssets(String organizationId, String employeeId) {
        String sql = "SELECT a.* FROM Asset a " +
                     "JOIN EmployeeAssets ea ON a.AssetID = ea.AssetID " +
                     "WHERE ea.OrganizationID = ? AND ea.EmployeeID = ?";

        Map<String, Map<String, Object>> assetsMap = new HashMap<>();
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String assetId = String.valueOf(resultSet.getInt("AssetID"));
                Map<String, Object> assetDetails = new HashMap<>();

                assetDetails.put("Name", resultSet.getString("Name"));
                assetDetails.put("Price", resultSet.getString("Price"));
                assetDetails.put("PurchaseDate", resultSet.getString("PurchaseDate"));
                assetDetails.put("Status", resultSet.getString("Status"));
                // Add more fields if present in the Assets table
                // e.g., assetDetails.put("FieldName", resultSet.getString("FieldName"));

                assetsMap.put(assetId, assetDetails);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assetsMap;
    }

    public static boolean updateAsset(String organizationId,String assetId, Map<String, Object> updates) {
        if (updates.isEmpty()) {
            return false; // No fields to update
        }
    
        StringBuilder sql = new StringBuilder("UPDATE Asset SET ");
        Iterator<Map.Entry<String, Object>> iterator = updates.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            sql.append(entry.getKey()).append(" = ?");
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        
        sql.append(" WHERE OrganizationID = ? AND AssetID = ?");
    
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            int index = 1;
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if(entry.getKey().toString().equalsIgnoreCase("PRICE"))
                    statement.setObject(index++, Double.parseDouble(entry.getValue().toString()));
                else
                    statement.setObject(index++, entry.getValue());
            }
            statement.setInt(index++, Integer.parseInt(organizationId));
            statement.setInt(index++, Integer.parseInt(assetId));

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteAsset(String organizationId, String assetId) {
        String sql = "DELETE FROM Asset WHERE OrganizationID = ? AND AssetID = ?";
        
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(assetId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean assignEmployee(String organizationId, String employeeId,String assetId) {
        String sql = "INSERT INTO EmployeeAssets VALUES (?, ?, ?)";
        
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setInt(3, Integer.parseInt(assetId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean unassignEmployee(String organizationId, String employeeId,String assetId) {
        String sql = "DELETE FROM EmployeeAssets where OrganizationID = ? AND EmployeeID = ? AND AssetID = ?";
        
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setInt(3, Integer.parseInt(assetId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }      
}  