package DBUtil;

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

public class DBHandler {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/EMS";
    private static final String USERNAME = "Temp";
    private static final String PASSWORD = "123456";

    // Method to establish a connection to the database
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public String getRoleName(String employeeID, String organizationID) {
        String roleName = null;
        String query = "SELECT r.Name AS RoleName FROM Roles r " +
                       "JOIN Employee e ON r.OrganizationID = e.OrganizationID AND r.RoleID = e.RoleID " +
                       "WHERE e.EmployeeID = ? AND e.OrganizationID = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
             
            preparedStatement.setString(1, employeeID);
            preparedStatement.setString(2, organizationID);
    
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                roleName = resultSet.getString("RoleName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roleName;
    }
    
    public Map<String, String> getOrganizationAndEmployeeId(String organizationName, String Name,String emailID) {
        String orgSql = "SELECT OrganizationID FROM Organizations WHERE OrganizationName = ?";
        String empSql = "SELECT EmployeeID FROM Employee WHERE Name = ? AND OrganizationID = ? AND emailID = ?";
        Map<String, String> result = new HashMap<>();
        int organizationId = -1;
    
        // Step 1: Get OrganizationID using the organization name
        try (Connection connection = getConnection();
             PreparedStatement orgStatement = connection.prepareStatement(orgSql)) {
    
            orgStatement.setString(1, organizationName);
            ResultSet orgResultSet = orgStatement.executeQuery();
    
            if (orgResultSet.next()) {
                organizationId = orgResultSet.getInt("OrganizationID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return result; // Return empty result in case of error
        }
    
        // Step 2: Get EmployeeID using the first name and OrganizationID
        if (organizationId !=-1) {
            try (Connection connection = getConnection();
                 PreparedStatement empStatement = connection.prepareStatement(empSql)) {
    
                empStatement.setString(1, Name);
                empStatement.setInt(2, organizationId);
                empStatement.setString(3, emailID);
                ResultSet empResultSet = empStatement.executeQuery();
    
                if (empResultSet.next()) {
                    result.put("OrganizationID", String.valueOf(organizationId));
                    result.put("EmployeeID", String.valueOf(empResultSet.getInt("EmployeeID")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        return result;
    }
    

    public Map<String, Object> readOrganization(String organizationId) {
        String sql = "SELECT * FROM Organizations WHERE OrganizationID = ?";
        Map<String, Object> organization = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, organizationId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                organization.put("OrganizationID", resultSet.getString("OrganizationID"));
                organization.put("Name", resultSet.getString("Name"));
                organization.put("Address", resultSet.getString("Address"));
                organization.put("StartDate", resultSet.getDate("StartDate"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return organization;
    }

    public String createOrganization(String name, String address, String startDate) {
        String maxIdSql = "SELECT MAX(OrganizationID) FROM Organizations";
        String insertSql = "INSERT INTO Organizations (OrganizationID, Name, Address, StartDate) VALUES (?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
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
    public boolean updateOrganization(String organizationId, Map<String, Object> updates) {
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

        try (Connection connection = getConnection();
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
    public boolean deleteOrganization(String organizationId) {
        // Define the SQL queries for deleting related records in the correct order
        String deleteEmployeesSql = "DELETE FROM Employee WHERE OrganizationID = ?";
        String deleteRolesSql = "DELETE FROM Roles WHERE OrganizationID = ?";
        String deleteDepartmentsSql = "DELETE FROM Departments WHERE OrganizationID = ?";
        String deleteOrganizationSql = "DELETE FROM Organizations WHERE OrganizationID = ?";
        String deleteOrganizationEmailsSql = "DELETE FROM OrganizationEmails WHERE OrganizationID = ?";
        String deleteOrganizationPhoneNumbersSql = "DELETE FROM OrganizationPhoneNumbers WHERE OrganizationID = ?";
        String deleteOrganizationExpensesSql = "DELETE FROM OrganizationExpenses WHERE OrganizationID = ?";
    
        try (Connection connection = getConnection()) {
            // Disable auto-commit to handle all deletions as a single transaction
            connection.setAutoCommit(false);
    
            try (
                PreparedStatement deleteEmployeesStmt = connection.prepareStatement(deleteEmployeesSql);
                PreparedStatement deleteRolesStmt = connection.prepareStatement(deleteRolesSql);
                PreparedStatement deleteDepartmentsStmt = connection.prepareStatement(deleteDepartmentsSql);
                PreparedStatement deleteOrganizationStmt = connection.prepareStatement(deleteOrganizationSql);
                PreparedStatement deleteOrganizationEmStmt = connection.prepareStatement(deleteOrganizationEmailsSql);
                PreparedStatement deleteOrganizationPhnStmt = connection.prepareStatement(deleteOrganizationPhoneNumbersSql);
                PreparedStatement deleteOrganizationExpStmt = connection.prepareStatement(deleteOrganizationExpensesSql);
            ) {
                // Set the OrganizationID parameter for each statement
                deleteEmployeesStmt.setInt(1, Integer.parseInt(organizationId));
                deleteRolesStmt.setInt(1, Integer.parseInt(organizationId));
                deleteDepartmentsStmt.setInt(1, Integer.parseInt(organizationId));
                deleteOrganizationStmt.setInt(1, Integer.parseInt(organizationId));
                deleteOrganizationEmStmt.setInt(1, Integer.parseInt(organizationId));
                deleteOrganizationPhnStmt.setInt(1, Integer.parseInt(organizationId));
                deleteOrganizationExpStmt.setInt(1, Integer.parseInt(organizationId));
    
                // Execute deletions in the required order
                deleteEmployeesStmt.executeUpdate(); // Delete employees
                deleteRolesStmt.executeUpdate();     // Delete roles
                deleteDepartmentsStmt.executeUpdate(); // Delete departments
                int affectedRows = deleteOrganizationStmt.executeUpdate(); // Delete the organization
                deleteOrganizationStmt.executeUpdate(); // Delete departments
                deleteOrganizationEmStmt.executeUpdate(); // Delete departments
                deleteOrganizationPhnStmt.executeUpdate(); // Delete departments
                deleteOrganizationExpStmt.executeUpdate(); // Delete departments
    
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
    


    // Read phone number details
    public Map<Integer, Object> readPhoneNumber(String organizationId) {
        String sql = "SELECT * FROM OrganizationPhoneNumbers WHERE OrganizationID = ?";
        Map<Integer, Object> phoneDatas = new HashMap<>();
        Map<String, Object> phoneData;

        int index = 1;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                phoneData = new HashMap<>();
                phoneData.put("OrganizationID", resultSet.getString("OrganizationID"));
                phoneData.put("PhoneNumber", resultSet.getString("PhoneNumber"));
                phoneData.put("Primary", String.valueOf(resultSet.getBoolean("isPrimary")));
                phoneDatas.put(index,phoneData.toString());
                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return phoneDatas;
    }

    // Create a new phone number entry
    public boolean createPhoneNumber(String organizationId, String phoneNumber, String isPrimary) {
        String sql = "INSERT INTO OrganizationPhoneNumbers (OrganizationID, PhoneNumber, isPrimary) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
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
    public boolean updatePhoneNumber(String organizationId, String OphoneNumber,String NphoneNumber, String Primary) {
        String sql = "UPDATE OrganizationPhoneNumbers Set PhoneNumber = ? ";

        if(Primary != null)
            sql+= ", isPrimary = ? ";
        int index = 1;
        sql+=" WHERE OrganizationID = ? AND PhoneNumber = ? ;";
        try (Connection connection = getConnection();
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
    public boolean deletePhoneNumber(String organizationId, String phoneNumber) {
        String sql = "DELETE FROM OrganizationPhoneNumbers WHERE OrganizationID = ? AND PhoneNumber = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setString(2, phoneNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




    // Create a new email for an organization
    public boolean createOrganizationEmail(int organizationId, String email) {
        String sql = "INSERT INTO OrganizationEmails (OrganizationID, Email) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, organizationId);
            statement.setString(2, email);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read email details for a specific organization
    public Map<Integer, Object> readOrganizationEmail(int organizationId) {
        String sql = "SELECT * FROM OrganizationEmails WHERE OrganizationID = ? ";
        Map<Integer, Object> results = new HashMap<>();
        Map<String, Object> result;
        int index = 1;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, organizationId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result = new HashMap<>();
                result.put("OrganizationID", rs.getInt("OrganizationID"));
                result.put("Email", rs.getString("Email"));
                results.put(index++,result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // Update an email for an organization
    public boolean updateOrganizationEmail(int organizationId, String oldEmail, String newEmail) {
        String sql = "UPDATE OrganizationEmails SET Email = ? WHERE OrganizationID = ? AND Email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newEmail);
            statement.setInt(2, organizationId);
            statement.setString(3, oldEmail);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an email for an organization
    public boolean deleteOrganizationEmail(int organizationId, String email) {
        String sql = "DELETE FROM OrganizationEmails WHERE OrganizationID = ? AND Email = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, organizationId);
            statement.setString(2, email);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createOrganizationExpense(int organizationId, int employeeId, int expenseId, int amount, String type, String date, String description) {
        String sql = "INSERT INTO OrganizationExpenses (OrganizationID, EmployeeID, ExpenseID, Amount, Type, Date, Description) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, organizationId);
            statement.setInt(2, employeeId);
            statement.setInt(3, expenseId);
            statement.setInt(4, amount);
            statement.setString(5, type);
            statement.setString(6, date);
            statement.setString(7, description);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Read expenses for a specific organization (can include multiple records)
    public Map<Integer, Map<String, Object>> readOrganizationExpenses(int organizationId,int EmployeeID) {
        String sql = "SELECT * FROM OrganizationExpenses WHERE OrganizationID = ?";
        Map<Integer, Map<String, Object>> results = new HashMap<>();
        int index = 1;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, organizationId);
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
                results.put(index++, result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // Update an expense record for a specific organization and employee
    public boolean updateOrganizationExpense(int organizationId, int employeeId, int expenseId, Map<String, Object> updates) {
        String sql = "UPDATE OrganizationExpenses SET Amount = ?, Type = ?, Date = ?, Description = ? WHERE OrganizationID = ? AND EmployeeID = ? AND ExpenseID = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, (Integer) updates.get("Amount"));
            statement.setString(2, (String) updates.get("Type"));
            statement.setString(3, (String) updates.get("Date"));
            statement.setString(4, (String) updates.get("Description"));
            statement.setInt(5, organizationId);
            statement.setInt(6, employeeId);
            statement.setInt(7, expenseId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an expense record for a specific organization and employee
    public boolean deleteOrganizationExpense(int organizationId, int employeeId, int expenseId) {
        String sql = "DELETE FROM OrganizationExpenses WHERE OrganizationID = ? AND EmployeeID = ? AND ExpenseID = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, organizationId);
            statement.setInt(2, employeeId);
            statement.setInt(3, expenseId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUser(String orgID, String empID,String password) {
        String empSql = "SELECT COUNT(*) AS count FROM Employee WHERE OrganizationID = ? AND employeeID = ? AND Password = ?";
        try (Connection connection = getConnection();
            PreparedStatement empStatement = connection.prepareStatement(empSql)) {
            
            empStatement.setInt(1, Integer.parseInt(orgID));
            empStatement.setInt(2, Integer.parseInt(empID));
            empStatement.setString(3, password);
            ResultSet empResultSet = empStatement.executeQuery();
            
            if (empResultSet.next() && empResultSet.getInt("count") > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Return false if user verification fails
    }
    

    public boolean isCEO(String employeeId, String organizationId) {
        try (Connection conn = getConnection()) {
            String query = "SELECT COUNT(*) FROM Employee e " +
                           "JOIN Role r ON e.RoleID = r.RoleID " +
                           "WHERE e.EmployeeID = ? AND e.OrganizationID = ? AND r.RoleName = 'CEO'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, employeeId);
            stmt.setString(2, organizationId);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isManager(String employeeId, String organizationId) {
        try (Connection conn = getConnection()) {
            String query = "SELECT COUNT(*) FROM Employee e " +
                           "JOIN Role r ON e.RoleID = r.RoleID " +
                           "WHERE e.EmployeeID = ? AND e.OrganizationID = ? AND r.RoleName = 'Manager'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, employeeId);
            stmt.setString(2, organizationId);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    public String createEmployee(String organizationID,String emailID, String Name,String dob, String doorNumber, String area, String city, String district, String country, String pinCode, String departmentID, String roleID, String joiningDate, String officeLocation, String managerID, String employmentType, String maritalStatus,String password) {
        // SQL to get the max EmployeeID for the given OrganizationID
        String getMaxIdSql = "SELECT MAX(EmployeeID) FROM Employee WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Employee (EmployeeID, OrganizationID, emailID, Name, DOB, DoorNumber, Area, City, District, Country, PINCode, DepartmentID, RoleID, JoiningDate, OfficeLocation, ManagerID, EmploymentType, MaritalStatus,Password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement maxIdStmt = connection.prepareStatement(getMaxIdSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            
            // Step 1: Get the maximum EmployeeID for the organization
            maxIdStmt.setInt(1, Integer.parseInt(organizationID));
            ResultSet resultSet = maxIdStmt.executeQuery();
            int newEmployeeId = 1; // Default to 1 if no employees exist
            
            if (resultSet.next()) {
                String maxEmployeeIdStr = resultSet.getString(1);
                if (maxEmployeeIdStr != null) {
                    newEmployeeId = Integer.parseInt(maxEmployeeIdStr) + 1;
                }
            }
            
            // Step 2: Insert the new employee with the incremented EmployeeID
            
            insertStmt.setInt(1, newEmployeeId);
            insertStmt.setInt(2, Integer.parseInt(organizationID));
            insertStmt.setString(3,  emailID);
            insertStmt.setString(4,  Name);
            insertStmt.setString(5, dob);
            insertStmt.setInt(6, Integer.parseInt(doorNumber));
            insertStmt.setString(7, area);
            insertStmt.setString(8, city);
            insertStmt.setString(9, district);
            insertStmt.setString(10, country);
            insertStmt.setString(11, pinCode);

            if (departmentID == null || departmentID.equalsIgnoreCase("null")) {
                insertStmt.setNull(12, java.sql.Types.INTEGER);
            } else {
                insertStmt.setInt(12, Integer.parseInt(departmentID));
            }

            if (roleID == null || roleID.equalsIgnoreCase("null")) {
                insertStmt.setNull(13, java.sql.Types.INTEGER);
            } else {
                insertStmt.setInt(13, Integer.parseInt(roleID));
            }
            insertStmt.setString(14, joiningDate);
            insertStmt.setString(15, officeLocation);
            if (managerID == null || managerID.equalsIgnoreCase("null")) {
                insertStmt.setNull(16, java.sql.Types.INTEGER);
            } else {
                insertStmt.setInt(16, Integer.parseInt(managerID));
            }
            insertStmt.setString(17, employmentType);
            insertStmt.setBoolean(18, (maritalStatus.equals("Single"))?true:false);
            insertStmt.setString(19, password);
            
            insertStmt.executeUpdate();
            return String.valueOf(newEmployeeId);
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }


    
    public Map<String, Object> readEmployee(String organizationID,String employeeId) {
        String sql = "SELECT * FROM Employee WHERE OrganizationID = ? AND EmployeeID = ?";
        Map<String, Object> employee = new HashMap<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeId));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                employee.put("EmployeeID", resultSet.getInt("EmployeeID"));
                employee.put("OrganizationID", resultSet.getInt("OrganizationID"));
                employee.put("EmailID", resultSet.getString("EmailID"));
                employee.put("Name", resultSet.getString("Name"));
                employee.put("DOB", resultSet.getString("DOB"));
                employee.put("DoorNumber", resultSet.getInt("DoorNumber"));
                employee.put("Area", resultSet.getString("Area"));
                employee.put("City", resultSet.getString("City"));
                employee.put("District", resultSet.getString("District"));
                employee.put("Country", resultSet.getString("Country"));
                employee.put("PINCode", resultSet.getString("PINCode"));
                if(resultSet.getInt("DepartmentID") == 0)
                    employee.put("DepartmentID", "null");
                else
                    employee.put("DepartmentID", resultSet.getInt("DepartmentID"));
                if(resultSet.getInt("RoleID") == 0)
                    employee.put("RoleID", "null");
                else
                    employee.put("RoleID", resultSet.getInt("RoleID"));
                employee.put("JoiningDate", resultSet.getString("JoiningDate"));
                employee.put("OfficeLocation", resultSet.getString("OfficeLocation"));
                if(resultSet.getInt("ManagerID") == 0)
                    employee.put("ManagerID", "null");
                else
                    employee.put("ManagerID", resultSet.getInt("ManagerID"));
                employee.put("EmploymentType", resultSet.getString("EmploymentType"));
                employee.put("MaritalStatus", resultSet.getBoolean("MaritalStatus"));
                employee.put("Password", resultSet.getString("Password"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employee;
    }

    public boolean updateEmployee(String organizationID,String employeeId, Map<String, Object> updates) {
        if (updates.isEmpty()) {
            return false; // No fields to update
        }
    
        StringBuilder sql = new StringBuilder("UPDATE Employee SET ");
        Iterator<Map.Entry<String, Object>> iterator = updates.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            sql.append(entry.getKey()).append(" = ?");
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }
        
        sql.append(" WHERE OrganizationID = ? AND EmployeeID = ?");
    
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            
            int index = 1;
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                statement.setObject(index++, entry.getValue());
            }
            statement.setInt(index++, Integer.parseInt(organizationID));
            statement.setInt(index, Integer.parseInt(employeeId));
    
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEmployee(String employeeId, String organizationId) {
        String sql = "DELETE FROM Employee WHERE EmployeeID = ? AND OrganizationID = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String createDepartment(String organizationId, String departmentName, String managerId) {
        // Query to get the max DepartmentID for the given OrganizationID
        String getMaxIdSql = "SELECT MAX(DepartmentID) AS maxId FROM Departments WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Departments (OrganizationID, DepartmentID, DepartmentName, ManagerID) VALUES (?, ?, ?, ?)";
    
        try (Connection connection = getConnection();
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
    public Map<String, Object> readDepartment(String organizationId, String departmentId) {
        String sql = "SELECT * FROM Departments WHERE OrganizationID = ? AND DepartmentID = ?";
        Map<String, Object> departmentData = new HashMap<>();
        try (Connection connection = getConnection();
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
    public boolean updateDepartment(String organizationId, String departmentId, Map<String, Object> updates) {
        StringBuilder sql = new StringBuilder("UPDATE Departments SET ");
        boolean first = true;

        for (String key : updates.keySet()) {
            if (!first) sql.append(", ");
            sql.append(key).append(" = ?");
            first = false;
        }
        sql.append(" WHERE OrganizationID = ? AND DepartmentID = ?");

        try (Connection connection = getConnection();
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
    public boolean deleteDepartment(String organizationId, String departmentId) {
        String sql = "DELETE FROM Departments WHERE OrganizationID = ? AND DepartmentID = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, organizationId);
            statement.setString(2, departmentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Read operation
    public Map<String, Object> readRole(String organizationId, String roleId) {
        String sql = "SELECT * FROM Roles WHERE OrganizationID = ? AND RoleID = ?";
        Map<String, Object> roleData = new HashMap<>();

        try (Connection connection = getConnection();
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
    public String createRole(String organizationId, String name) {
        // Query to get the max RoleID for the given OrganizationID
        String getMaxIdSql = "SELECT MAX(RoleID) AS maxId FROM Roles WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Roles (OrganizationID, RoleID, Name) VALUES (?, ?, ?)";

        int orgID = Integer.parseInt(organizationId);
        
        try (Connection connection = getConnection();
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
    public boolean updateRole(String organizationId, String roleId, Map<String, Object> updates) {
        StringBuilder sql = new StringBuilder("UPDATE Roles SET ");
        int count = 0;

        for (String key : updates.keySet()) {
            if (count > 0) sql.append(", ");
            sql.append(key).append(" = ?");
            count++;
        }

        sql.append(" WHERE OrganizationID = ? AND RoleID = ?");

        try (Connection connection = getConnection();
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
    public boolean deleteRole(String organizationId, String roleId) {
        String sql = "DELETE FROM Roles WHERE OrganizationID = ? AND roleID = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(roleId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}