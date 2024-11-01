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

public class OrganizationsDAO {
    public static Map<String, Object> readOrganization(String organizationId) {
        String sql = "SELECT * FROM Organizations WHERE OrganizationID = ?";
        Map<String, Object> organization = new HashMap<>();

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationId));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                organization.put("OrganizationID", resultSet.getString("OrganizationID"));
                organization.put("Name", resultSet.getString("Name"));
                organization.put("Address", resultSet.getString("Address"));
                organization.put("StartDate", resultSet.getString("StartDate"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return organization;
    }

    public static String createOrganization(String name, String address, String startDate) {
        String maxIdSql = "SELECT MAX(OrganizationID) FROM Organizations";
        String insertSql = "INSERT INTO Organizations (OrganizationID, Name, Address, StartDate) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = DB.getConnection();
             PreparedStatement maxIdStatement = connection.prepareStatement(maxIdSql);
             ResultSet resultSet = maxIdStatement.executeQuery()) {
    
            // Get the current maximum OrganizationID
            int newOrganizationId = 1; // Default value if no records exist
            if (resultSet.next()) {
                // If there are existing records, increment the max ID by 1
                int maxId = resultSet.getInt(1);
                newOrganizationId = maxId + 1;
            }
    
            // Prepare the insert statement with the new OrganizationID
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStatement.setInt(1, newOrganizationId); // Use int for OrganizationID
                insertStatement.setString(2, name);
                insertStatement.setString(3, address);
                insertStatement.setString(4, startDate);
                insertStatement.executeUpdate();
            }
            return String.valueOf(newOrganizationId);
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }
    public static boolean updateOrganization(String organizationId, Map<String, Object> updates) {
        StringBuilder sql = new StringBuilder("UPDATE Organizations SET ");
        boolean first = true;

        // Iterate through the updates map to build the SQL query
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(entry.getKey()).append(" = ?");
            first = false;
        }

        sql.append(" WHERE OrganizationID = ?");

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;

            // Set the values from the updates map
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                statement.setObject(index++, entry.getValue());
            }

            // Set the organization ID for the WHERE clause
            statement.setInt(index, Integer.parseInt(organizationId));

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteOrganization(String organizationId) {
        // Define the SQL queries for deleting related records in the correct order
        String deleteRolesSql = "DELETE FROM Roles WHERE OrganizationID = ?";
        String deleteDepartmentsSql = "DELETE FROM Departments WHERE OrganizationID = ?";
        String deleteOrganizationEmailsSql = "DELETE FROM OrganizationEmails WHERE OrganizationID = ?";
        String deleteOrganizationPhoneNumbersSql = "DELETE FROM OrganizationPhoneNumbers WHERE OrganizationID = ?";
        String deleteOrganizationExpensesSql = "DELETE FROM OrganizationExpenses WHERE OrganizationID = ?";
        String deleteOrganizationAssetsSql = "DELETE FROM Asset WHERE OrganizationID = ?";
        String deleteOrganizationSql = "DELETE FROM Organizations WHERE OrganizationID = ?";

        int orgId = Integer.parseInt(organizationId);

        EmployeeDAO.deleteEmployees(organizationId);

        try (Connection connection = DB.getConnection()) {
            // Disable auto-commit to handle all deletions as a single transaction
            connection.setAutoCommit(false);
    
            try (
                PreparedStatement deleteRolesStmt = connection.prepareStatement(deleteRolesSql);
                PreparedStatement deleteDepartmentsStmt = connection.prepareStatement(deleteDepartmentsSql);
                PreparedStatement deleteOrganizationEmStmt = connection.prepareStatement(deleteOrganizationEmailsSql);
                PreparedStatement deleteOrganizationPhnStmt = connection.prepareStatement(deleteOrganizationPhoneNumbersSql);
                PreparedStatement deleteOrganizationExpStmt = connection.prepareStatement(deleteOrganizationExpensesSql);
                PreparedStatement deleteOrganizationAsstStmt = connection.prepareStatement(deleteOrganizationAssetsSql);
                PreparedStatement deleteOrganizationStmt = connection.prepareStatement(deleteOrganizationSql);
            ) {
                // Set the OrganizationID parameter for each statement
                deleteRolesStmt.setInt(1, orgId);
                deleteDepartmentsStmt.setInt(1, orgId);
                deleteOrganizationEmStmt.setInt(1, orgId);
                deleteOrganizationPhnStmt.setInt(1, orgId);
                deleteOrganizationExpStmt.setInt(1, orgId);
                deleteOrganizationAsstStmt.setInt(1, orgId);
                deleteOrganizationStmt.setInt(1, orgId);
    
                // Execute deletions in the required order
                deleteRolesStmt.executeUpdate();     // Delete roles
                deleteDepartmentsStmt.executeUpdate(); // Delete departments
                deleteOrganizationEmStmt.executeUpdate(); // Delete departments
                deleteOrganizationPhnStmt.executeUpdate(); // Delete departments
                deleteOrganizationExpStmt.executeUpdate(); // Delete departments
                deleteOrganizationAsstStmt.executeUpdate(); // Delete departments
                int affectedRows = deleteOrganizationStmt.executeUpdate(); // Delete the organization
    
                // If everything went well, commit the transaction
                connection.commit();
    
                // Return true if the organization record was successfully deleted
                return affectedRows > 0;
            } 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean doesOrganizationExist(String organizationId) {
        String sql = "SELECT 1 FROM Organizations WHERE OrganizationID = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Returns true if at least one record exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Read phone number details
    public static Map<Integer, Object> readPhoneNumbers(String organizationId) {
        String sql = "SELECT * FROM OrganizationPhoneNumbers WHERE OrganizationID = ?";
        Map<Integer, Object> phoneDatas = new HashMap<>();
        Map<String, Object> phoneData;

        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                phoneData = new HashMap<>();
                phoneData.put("OrganizationID", resultSet.getString("OrganizationID"));
                phoneData.put("PhoneNumber", resultSet.getString("PhoneNumber"));
                phoneData.put("Primary", String.valueOf(resultSet.getBoolean("isPrimary")));
                phoneDatas.put(index,phoneData);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return phoneDatas;
    }

    public static Map<Integer, Object> readPrimaryPhoneNumbers(String organizationId) {
        String sql = "SELECT * FROM OrganizationPhoneNumbers WHERE OrganizationID = ? AND isPrimary = true";
        Map<Integer, Object> phoneDatas = new HashMap<>();
        Map<String, Object> phoneData;

        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                phoneData = new HashMap<>();
                phoneData.put("OrganizationID", resultSet.getString("OrganizationID"));
                phoneData.put("PhoneNumber", resultSet.getString("PhoneNumber"));
                phoneData.put("Primary", String.valueOf(resultSet.getBoolean("isPrimary")));
                phoneDatas.put(index,phoneData);
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return phoneDatas;
    }

    // Create a new phone number entry
    public static boolean createPhoneNumber(String organizationId, String phoneNumber, String isPrimary) {
        String sql = "INSERT INTO OrganizationPhoneNumbers (OrganizationID, PhoneNumber, isPrimary) VALUES (?, ?, ?)";

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setString(2, phoneNumber);
            statement.setBoolean(3, (isPrimary.toLowerCase().equals("true")));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing phone number entry
    public static boolean updatePhoneNumber(String organizationId, String OphoneNumber,String NphoneNumber, String Primary) {
        String sql = "UPDATE OrganizationPhoneNumbers Set PhoneNumber = ? ";

        if(Primary != null)
            sql+= ", isPrimary = ? ";
        int index = 1;
        sql+=" WHERE OrganizationID = ? AND PhoneNumber = ? ;";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(index++, NphoneNumber);
            if(Primary != null)
                statement.setBoolean(index++, (Boolean)(Primary.toLowerCase().equals("true")));
            statement.setInt(index++, Integer.parseInt(organizationId));
            statement.setString(index++, OphoneNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a phone number entry
    public static boolean deletePhoneNumber(String organizationId, String phoneNumber) {
        String sql = "DELETE FROM OrganizationPhoneNumbers WHERE OrganizationID = ? AND PhoneNumber = ?";

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setString(2, phoneNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createOrganizationEmail(String organizationId, String email) {
        String getMaxEmailIdSql = "SELECT COALESCE(MAX(EmailID), 0) + 1 AS NewEmailID FROM OrganizationEmails WHERE OrganizationID = ?";
        String insertEmailSql = "INSERT INTO OrganizationEmails (OrganizationID, EmailID, Email) VALUES (?, ?, ?)";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement getMaxStatement = connection.prepareStatement(getMaxEmailIdSql);
            PreparedStatement insertStatement = connection.prepareStatement(insertEmailSql)) {
            
            getMaxStatement.setInt(1, Integer.parseInt(organizationId));
            int newEmailId = 1; 
            
            try (ResultSet resultSet = getMaxStatement.executeQuery()) {
                if (resultSet.next()) {
                    newEmailId = resultSet.getInt("NewEmailID");
                }
            }
            
            insertStatement.setInt(1, Integer.parseInt(organizationId));
            insertStatement.setInt(2, newEmailId);
            insertStatement.setString(3, email);
            
            return insertStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Map<String, Object> readOrganizationEmail(String organizationId,String emailId) {
        String sql = "SELECT * FROM OrganizationEmails WHERE OrganizationID = ? AND emailID = ?";
        Map<String, Object> result = new HashMap<>();
        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(emailId));
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                result.put("OrganizationID", rs.getInt("OrganizationID"));
                result.put("EmailID", rs.getInt("EmailID"));
                result.put("Email", rs.getString("Email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Map<Integer, Object> readOrganizationEmails(String organizationId) {
        String sql = "SELECT * FROM OrganizationEmails WHERE OrganizationID = ? ";
        Map<Integer, Object> results = new HashMap<>();
        Map<String, Object> result;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result = new HashMap<>();
                result.put("OrganizationID", rs.getInt("OrganizationID"));
                result.put("Email", rs.getString("Email"));
                results.put(rs.getInt("EmailID"),result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // Update an email for an organization
    public static boolean updateOrganizationEmail(String organizationId, String emailId, String email) {
        String sql = "UPDATE OrganizationEmails SET Email = ? WHERE OrganizationID = ? AND EmailID = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1,email);
            statement.setInt(2, Integer.parseInt(organizationId));
            statement.setInt(3, Integer.parseInt(emailId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteOrganizationEmail(String organizationId, String emailId) {
        String sql = "DELETE FROM OrganizationEmails WHERE OrganizationID = ? AND EmailID = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(emailId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createOrganizationExpense(String organizationId, String employeeId, String amount, String type, String date, String description) {
        String getMaxExpenseIdSql = "SELECT COALESCE(MAX(ExpenseID), 0) FROM OrganizationExpenses WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO OrganizationExpenses (OrganizationID, EmployeeID, ExpenseID, Amount, Type, Date, Description) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DB.getConnection()) {
            // Step 1: Get the current maximum ExpenseID for the organization
            int newExpenseId = 1; // Default value in case there are no expenses yet.
            try (PreparedStatement getMaxIdStatement = connection.prepareStatement(getMaxExpenseIdSql)) {
                getMaxIdStatement.setInt(1, Integer.parseInt(organizationId));
                ResultSet resultSet = getMaxIdStatement.executeQuery();
                if (resultSet.next()) {
                    newExpenseId = resultSet.getInt(1) + 1; // Increment the max value by 1
                }
            }

            // Step 2: Insert the new expense with the incremented ExpenseID
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStatement.setInt(1, Integer.parseInt(organizationId));
                insertStatement.setInt(2, Integer.parseInt(employeeId));
                insertStatement.setInt(3, newExpenseId);
                insertStatement.setDouble(4, Double.parseDouble(amount));
                insertStatement.setString(5, type);
                insertStatement.setString(6, date);
                insertStatement.setString(7, description);
                return insertStatement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Read expenses for a specific organization (can include multiple records)
    public static Map<Integer, Map<String, Object>> readOrganizationExpenses(String organizationId,String EmployeeId) {
        String sql = "SELECT * FROM OrganizationExpenses WHERE OrganizationID = ? AND EmployeeID = ?";
        Map<Integer, Map<String, Object>> results = new HashMap<>();
        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(EmployeeId));
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("OrganizationID", rs.getInt("OrganizationID"));
                result.put("EmployeeID", rs.getInt("EmployeeID"));
                result.put("ExpenseID", rs.getInt("ExpenseID"));
                result.put("Amount", rs.getInt("Amount"));
                result.put("Type", rs.getString("Type"));
                result.put("Date", rs.getString("Date"));
                result.put("Description", rs.getString("Description"));
                results.put(rs.getInt("ExpenseID"), result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static Map<String, Object> readOrganizationExpense(String organizationId,String employeeId,String expenseId) {
        String sql = "SELECT * FROM OrganizationExpenses WHERE OrganizationID = ? AND EmployeeID = ? AND ExpenseId = ?";
        Map<String, Object> result = new HashMap<>();
        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setInt(3, Integer.parseInt(expenseId));
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {   
                result.put("OrganizationID", rs.getInt("OrganizationID"));
                result.put("EmployeeID", rs.getInt("EmployeeID"));
                result.put("ExpenseID", rs.getInt("ExpenseID"));
                result.put("Amount", rs.getInt("Amount"));
                result.put("Type", rs.getString("Type"));
                result.put("Date", rs.getString("Date"));
                result.put("Description", rs.getString("Description"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean updateOrganizationExpense(String organizationId, String employeeId, String expenseId, Map<String, Object> updates) {
        // Start building the SQL query
        StringBuilder sql = new StringBuilder("UPDATE OrganizationExpenses SET ");
        
        // Build the SET clause dynamically based on the keys in the map
        for (String key : updates.keySet()) {
            sql.append(key).append(" = ?, ");
        }
        
        // Remove the last comma and space, and add the WHERE clause
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE OrganizationID = ? AND EmployeeID = ? AND ExpenseID = ?");
        
        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            // Set the values for the columns dynamically
            int parameterIndex = 1;
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                statement.setObject(parameterIndex++, entry.getValue());
            }
            
            // Set the values for the WHERE clause
            statement.setInt(parameterIndex++, Integer.parseInt(organizationId));
            statement.setInt(parameterIndex++, Integer.parseInt(employeeId));
            statement.setInt(parameterIndex, Integer.parseInt(expenseId));
            
            // Execute the update and return the result
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an expense record for a specific organization and employee
    public static boolean deleteOrganizationExpense(String organizationId, String expenseId) {
        String sql = "DELETE FROM OrganizationExpenses WHERE OrganizationID = ? AND ExpenseID = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(expenseId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}