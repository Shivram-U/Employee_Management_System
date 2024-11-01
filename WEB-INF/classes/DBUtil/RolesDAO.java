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

public class RolesDAO {

    public static Map<String, Object> readRole(String organizationId, String roleId) {
        String sql = "SELECT * FROM Roles WHERE OrganizationID = ? AND RoleID = ?";
        Map<String, Object> roleData = new HashMap<>();

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(roleId));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                roleData.put("OrganizationID", resultSet.getInt("OrganizationID"));
                roleData.put("RoleID", resultSet.getInt("RoleID"));
                roleData.put("Name", resultSet.getString("Name"));
            } else {
                return null; // Role not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roleData;
    }

    // Create operation
    public static String createRole(String organizationId, String name) {
        // Query to get the max RoleID for the given OrganizationID
        String getMaxIdSql = "SELECT MAX(RoleID) AS maxId FROM Roles WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Roles (OrganizationID, RoleID, Name) VALUES (?, ?, ?)";

        int orgID = Integer.parseInt(organizationId);
        
        try (Connection connection = DB.getConnection();
            PreparedStatement getMaxIdStmt = connection.prepareStatement(getMaxIdSql);
            PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            // Get the maximum RoleID for the given OrganizationID
            getMaxIdStmt.setInt(1, orgID);
            ResultSet resultSet = getMaxIdStmt.executeQuery();
            int newRoleId = 1; // Default to 1 if no existing role IDs are found

            if (resultSet.next()) {
                int maxId = resultSet.getInt("maxId");
                // Increment the maximum RoleID by 1 for the new role
                newRoleId = maxId + 1;
            }

            // Set parameters for the insert statement
            insertStmt.setInt(1, orgID);
            insertStmt.setInt(2, newRoleId);
            insertStmt.setString(3, name);

            // Execute the insert statement
            insertStmt.executeUpdate();
            return String.valueOf(newRoleId);
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    // Update operation
    public static boolean updateRole(String organizationId, String roleId, Map<String, Object> updates) {
        StringBuilder sql = new StringBuilder("UPDATE Roles SET ");
        int count = 0;

        for (String key : updates.keySet()) {
            if (count > 0) sql.append(", ");
            sql.append(key).append(" = ?");
            count++;
        }

        sql.append(" WHERE OrganizationID = ? AND RoleID = ?");

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            int index = 1;
            for (String key : updates.keySet()) {
                if (updates.get(key) == null) {
                    statement.setNull(index++, java.sql.Types.VARCHAR);
                } else {
                    statement.setObject(index++, updates.get(key));
                }
            }
            statement.setString(index++, organizationId);
            statement.setString(index, roleId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Delete a department
    public static boolean deleteRole(String organizationId, String roleId) {
        String sql = "DELETE FROM Roles WHERE OrganizationID = ? AND roleID = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(roleId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean assignRoleToEmployee(String orgId, String empId, String roleId) {
        String sql = "UPDATE Employee SET RoleID = ? WHERE OrganizationID = ? AND EmployeeID = ?";
        boolean isSuccess = false;

        try (Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleId);
            stmt.setString(2, orgId);
            stmt.setString(3, empId);
            int rowsAffected = stmt.executeUpdate();

            isSuccess = rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }
    public static boolean removeEmployeeRole(String orgId, String empId) {
        String sql = "UPDATE Employee SET RoleID = NULL WHERE OrganizationID = ? AND EmployeeID = ?";
        boolean isSuccess = false;

        try (Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orgId);
            stmt.setString(2, empId);
            int rowsAffected = stmt.executeUpdate();

            isSuccess = rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

}