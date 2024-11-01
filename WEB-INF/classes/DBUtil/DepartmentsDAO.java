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
public class DepartmentsDAO {

    public static String createDepartment(String organizationId, String departmentName, String managerId) {
        // Query to get the max DepartmentID for the given OrganizationID
        String getMaxIdSql = "SELECT MAX(DepartmentID) AS maxId FROM Departments WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Departments (OrganizationID, DepartmentID, DepartmentName, ManagerID) VALUES (?, ?, ?, ?)";
    
        try (Connection connection = DB.getConnection();
             PreparedStatement getMaxIdStmt = connection.prepareStatement(getMaxIdSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
    
            // Get the maximum DepartmentID for the given OrganizationID
            getMaxIdStmt.setString(1, organizationId);
            ResultSet resultSet = getMaxIdStmt.executeQuery();
            int newDepartmentId = 1; // Default to 1 if no existing department IDs are found
    
            if (resultSet.next()) {
                int maxId = resultSet.getInt("maxId");
                // Increment the maximum DepartmentID by 1 for the new department
                newDepartmentId = maxId + 1;
            }
    
            // Set parameters for the insert statement
            insertStmt.setString(1, organizationId);
            insertStmt.setInt(2, newDepartmentId);
            insertStmt.setString(3, departmentName);
    
            // Check if managerId is "null" (as a string), set to SQL NULL if true
            if (managerId == null || managerId.equalsIgnoreCase("null")) {
                insertStmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                insertStmt.setString(4, managerId);
            }
    
            // Execute the insert statement
            insertStmt.executeUpdate();
            return String.valueOf(newDepartmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }
    


    // Read a department
    public static Map<String, Object> readDepartment(String organizationId, String departmentId) {
        String sql = "SELECT * FROM Departments WHERE OrganizationID = ? AND DepartmentID = ?";
        Map<String, Object> departmentData = new HashMap<>();
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, organizationId);
            statement.setString(2, departmentId);
            ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    departmentData.put("OrganizationID", resultSet.getInt("OrganizationID"));
                    departmentData.put("DepartmentID", resultSet.getInt("DepartmentID"));
                    departmentData.put("DepartmentName", resultSet.getString("DepartmentName"));
                    if(resultSet.getInt("ManagerID") == 0)
                        departmentData.put("ManagerID", "null");
                    else
                        departmentData.put("ManagerID", resultSet.getInt("ManagerID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departmentData;
    }

    // Update a department
    public static boolean updateDepartment(String organizationId, String departmentId, Map<String, Object> updates) {
        StringBuilder sql = new StringBuilder("UPDATE Departments SET ");
        boolean first = true;

        for (String key : updates.keySet()) {
            if (!first) sql.append(", ");
            sql.append(key).append(" = ?");
            first = false;
        }
        sql.append(" WHERE OrganizationID = ? AND DepartmentID = ?");

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object value : updates.values()) {
                statement.setObject(index++, value);
            }
            statement.setString(index++, organizationId);
            statement.setString(index, departmentId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a department
    public static boolean deleteDepartment(String organizationId, String departmentId) {
        String sql = "DELETE FROM Departments WHERE OrganizationID = ? AND DepartmentID = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, organizationId);
            statement.setString(2, departmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean assignDepartmentToEmployee(String orgId, String empId, String deptId) {
        String sql = "UPDATE Employee SET DepartmentID = ? WHERE OrganizationID = ? AND EmployeeID = ?";
        boolean isSuccess = false;

        try (Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deptId);
            stmt.setString(2, orgId);
            stmt.setString(3, empId);
            int rowsAffected = stmt.executeUpdate();

            isSuccess = rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSuccess;
    }

    public static boolean removeEmployeeFromDepartment(String orgId, String empId) {
        String sql = "UPDATE Employee SET DepartmentID = NULL WHERE OrganizationID = ? AND EmployeeID = ?";
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