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


public class EmployeeDAO {
    public static boolean verifyUser(String orgID, String empID,String password) {
        String empSql = "SELECT COUNT(*) AS count FROM Employee WHERE OrganizationID = ? AND employeeID = ? AND Password = ?";
        try (Connection connection = DB.getConnection();
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
    
    public static String getRoleName(String employeeID, String organizationID) {
        String roleName = null;
        String query = "SELECT r.Name AS RoleName FROM Roles r " +
                       "JOIN Employee e ON r.OrganizationID = e.OrganizationID AND r.RoleID = e.RoleID " +
                       "WHERE e.EmployeeID = ? AND e.OrganizationID = ?";
        try (Connection connection = DB.getConnection();
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
        return String.valueOf(roleName);
    }
    
    public static Map<String, Object> getEmployeeDepartment(String empId) {
        String sql = "SELECT * FROM Departments WHERE DepartmentID = (SELECT DepartmentID FROM Employees WHERE EmployeeID = ?)";
        Map<String, Object> department = new HashMap<>();

        try (Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, empId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                department.put("DepartmentID", rs.getString("DepartmentID"));
                department.put("DepartmentName", rs.getString("DepartmentName"));
                department.put("ManagerID", rs.getString("ManagerID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return department;
    }


    public static String createEmployee(String organizationID,String emailID, String Name,String dob, String doorNumber, String area, String city, String district, String country, String pinCode, String departmentID, String roleID, String joiningDate, String officeLocation, String managerID, String employmentType, String maritalStatus,String password) {
        // SQL to get the max EmployeeID for the given OrganizationID
        String getMaxIdSql = "SELECT MAX(EmployeeID) FROM Employee WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Employee (EmployeeID, OrganizationID, emailID, Name, DOB, DoorNumber, Area, City, District, Country, PINCode, DepartmentID, RoleID, JoiningDate, OfficeLocation, ManagerID, EmploymentType, MaritalStatus,Password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = DB.getConnection();
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
            
            if (departmentID == null || departmentID.equals("") || departmentID.equalsIgnoreCase("null")) {
                insertStmt.setNull(12, java.sql.Types.INTEGER);
            } else {
                insertStmt.setInt(12, Integer.parseInt(departmentID));
            }
            
            if (roleID == null || roleID.equals("") || roleID.equalsIgnoreCase("null")) {
                insertStmt.setNull(13, java.sql.Types.INTEGER);
            } else {
                insertStmt.setInt(13, Integer.parseInt(roleID));
            }
            
            insertStmt.setString(14, joiningDate);
            insertStmt.setString(15, officeLocation);
            
            if (managerID == null || managerID.equals("") || managerID.equalsIgnoreCase("null")) {
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


    
    public static Map<String, Object> readEmployee(String organizationID,String employeeId) {
        String sql = "SELECT * FROM Employee WHERE OrganizationID = ? AND EmployeeID = ?";
        Map<String, Object> employee = new HashMap<>();

        try (Connection connection = DB.getConnection();
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

    public static boolean updateEmployee(String organizationID,String employeeId, Map<String, Object> updates) {
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
    
        try (Connection connection = DB.getConnection();
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

    public static boolean deleteEmployees(String organizationId) {
        String sql = "DELETE FROM Employee WHERE OrganizationID = ?";
        String psql = "DELETE FROM EmployeePhoneNumbers WHERE OrganizationID = ?";
        String esql = "DELETE FROM EmployeeEMails WHERE OrganizationID = ?";
        String easstsql = "DELETE FROM EmployeeAssets WHERE OrganizationID = ?";
        String prsql = "DELETE FROM Payroll WHERE OrganizationID = ?";

        try (Connection connection = DB.getConnection()) {
            try (
                PreparedStatement stmt = connection.prepareStatement(sql);
                PreparedStatement pstmt = connection.prepareStatement(esql);
                PreparedStatement estmt = connection.prepareStatement(psql);
                PreparedStatement easststmt = connection.prepareStatement(easstsql);
                PreparedStatement prstmt = connection.prepareStatement(prsql);
            ) {
                // Set the OrganizationID parameter for each statement
                pstmt.setInt(1, Integer.parseInt(organizationId));
                estmt.setInt(1, Integer.parseInt(organizationId));
                easststmt.setInt(1, Integer.parseInt(organizationId));
                prstmt.setInt(1, Integer.parseInt(organizationId));
                stmt.setInt(1, Integer.parseInt(organizationId));

    
                //estmt.executeUpdate();
                //pstmt.executeUpdate();
                //prstmt.executeUpdate();
                //easststmt.executeUpdate();
                stmt.executeUpdate();
                // Return true if the organization record was successfully deleted
                return true;
            } 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEmployee(String employeeId, String organizationId) {
        String sql = "DELETE FROM Employee WHERE EmployeeID = ? AND OrganizationID = ?";
        String psql = "DELETE FROM EmployeePhoneNumbers WHERE EmployeeID = ? AND OrganizationID = ?";
        String esql = "DELETE FROM EmployeeEMails WHERE EmployeeID = ? AND OrganizationID = ?";
        String easstsql = "DELETE FROM EmployeeAssets WHERE OrganizationID = ?";
        
        try (Connection connection = DB.getConnection()) {
            // Disable auto-commit to handle all deletions as a single transaction
            connection.setAutoCommit(false);
    
            try (
                PreparedStatement stmt = connection.prepareStatement(sql);
                PreparedStatement pstmt = connection.prepareStatement(esql);
                PreparedStatement estmt = connection.prepareStatement(psql);
                PreparedStatement easststmt = connection.prepareStatement(easstsql);
            ) {
                // Set the OrganizationID parameter for each statement
                stmt.setInt(1, Integer.parseInt(employeeId));
                stmt.setInt(2, Integer.parseInt(organizationId));
                pstmt.setInt(1, Integer.parseInt(employeeId));
                pstmt.setInt(2, Integer.parseInt(organizationId));
                estmt.setInt(1, Integer.parseInt(employeeId));
                estmt.setInt(2, Integer.parseInt(organizationId));

                easststmt.setInt(1, Integer.parseInt(organizationId));

    
                estmt.executeUpdate();
                pstmt.executeUpdate();
                easststmt.executeUpdate();
                stmt.executeUpdate();
                
                // If everything went well, commit the transaction
                connection.commit();
    
                // Return true if the organization record was successfully deleted
                return true;
            } 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean doesEmployeeExist(String organizationId, String employeeId) {
        String sql = "SELECT 1 FROM Employee WHERE OrganizationID = ? AND EmployeeID = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Returns true if at least one record exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Read phone number details
    public static Map<Integer, Object> readPhoneNumbers(String organizationId,String employeeId) {
        String sql = "SELECT * FROM EmployeePhoneNumbers WHERE OrganizationID = ? AND employeeId = ?";
        Map<Integer, Object> phoneDatas = new HashMap<>();
        Map<String, Object> phoneData;

        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
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

    public static Map<Integer, Object> readPrimaryPhoneNumbers(String organizationId,String employeeId) {
        String sql = "SELECT * FROM EmployeePhoneNumbers WHERE OrganizationID = ? AND employeeId = ? and isPrimary = true";
        Map<Integer, Object> phoneDatas = new HashMap<>();
        Map<String, Object> phoneData;

        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
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
    public static boolean createPhoneNumber(String organizationId, String employeeId, String phoneNumber, String isPrimary) {
        String sql = "INSERT INTO EmployeePhoneNumbers (OrganizationID, EmployeeID, PhoneNumber, isPrimary) VALUES (?, ?, ?, ?)";

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setString(3, phoneNumber);
            statement.setBoolean(4, (isPrimary.toLowerCase().equals("true")));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update an existing phone number entry
    public static boolean updatePhoneNumber(String organizationId, String employeeId, String OphoneNumber,String NphoneNumber, String Primary) {
        String sql = "UPDATE EmployeePhoneNumbers Set PhoneNumber = ? ";

        if(Primary != null)
            sql+= ", isPrimary = ? ";
        int index = 1;
        sql+=" WHERE OrganizationID = ? AND EmployeeID = ? AND PhoneNumber = ? ;";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(index++, NphoneNumber);
            if(Primary != null)
                statement.setBoolean(index++, (Boolean)(Primary.toLowerCase().equals("true")));
            statement.setInt(index++, Integer.parseInt(organizationId));
            statement.setInt(index++, Integer.parseInt(employeeId));
            statement.setString(index++, OphoneNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete a phone number entry
    public static boolean deletePhoneNumber(String organizationId, String employeeId, String phoneNumber) {
        String sql = "DELETE FROM EmployeePhoneNumbers WHERE OrganizationID = ? AND EmployeeID = ? AND PhoneNumber = ?";

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setString(3, phoneNumber);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createEmail(String organizationId, String employeeId, String email) {
        String getMaxEmailIdSql = "SELECT MAX(EmailID) FROM EmployeeEmails WHERE OrganizationID = ? AND EmployeeID = ?";
        String insertEmailSql = "INSERT INTO EmployeeEmails (OrganizationID, EmployeeID, EmailID, Email) VALUES (?, ?, ?, ?)";

        try (Connection connection = DB.getConnection()) {
            // Step 1: Get the current maximum EmailID for the employee in the organization
            int maxEmailId = 0;
            try (PreparedStatement getMaxStmt = connection.prepareStatement(getMaxEmailIdSql)) {
                getMaxStmt.setInt(1, Integer.parseInt(organizationId));
                getMaxStmt.setInt(2, Integer.parseInt(employeeId));
                ResultSet rs = getMaxStmt.executeQuery();

                if (rs.next()) {
                    maxEmailId = rs.getInt(1); // Get the current max EmailID
                }
            }

            // Step 2: Increment the EmailID by 1 for the new email
            int newEmailId = maxEmailId + 1;

            // Step 3: Insert the new email with the incremented EmailID
            try (PreparedStatement insertStmt = connection.prepareStatement(insertEmailSql)) {
                insertStmt.setInt(1, Integer.parseInt(organizationId));
                insertStmt.setInt(2, Integer.parseInt(employeeId));
                insertStmt.setInt(3, newEmailId); // Set the new EmailID
                insertStmt.setString(4, email);
                return insertStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Read email details for a specific organization
    public static Map<String, Object> readEmail(String organizationId,String employeeId,String emailId) {
        String sql = "SELECT * FROM EmployeeEmails WHERE OrganizationID = ? AND EmployeeID = ? AND EmailID = ?";
        Map<String, Object> result = new HashMap<>();
        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setInt(3, Integer.parseInt(emailId));
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                result.put(emailId, rs.getString("Email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Map<Integer, Object> readEmails(String organizationId,String employeeId) {
        String sql = "SELECT * FROM EmployeeEmails WHERE OrganizationID = ? AND EmployeeID = ? ";
        Map<Integer, Object> results = new HashMap<>();
        Map<String, Object> result;
        int index = 1;
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                result = new HashMap<>();
                result.put("Email", rs.getString("Email"));
                results.put(rs.getInt("EmailID"),result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // Update an email for an organization
    public static boolean updateEmail(String organizationId, String employeeId, String emailId, String email) {
        String sql = "UPDATE EmployeeEmails SET Email = ? WHERE OrganizationID = ? AND EmployeeID = ? AND emailId = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, Integer.parseInt(organizationId));
            statement.setInt(3, Integer.parseInt(employeeId));
            statement.setInt(4, Integer.parseInt(emailId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete an email for an organization
    public static boolean deleteEmail(String organizationId,String employeeId, String emailId) {
        String sql = "DELETE FROM EmployeeEmails WHERE OrganizationID = ? AND EmployeeID = ? AND emailId = ?";
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(organizationId));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setInt(3, Integer.parseInt(emailId));
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String createEmployeeAttendance(String organizationID, String employeeID, String date, String leaveID) {
        String checkSql = "SELECT COUNT(*) FROM EmployeeAttendance WHERE OrganizationID = ? AND EmployeeID = ? AND Date = ?";
        String insertSql = "INSERT INTO EmployeeAttendance (OrganizationID, EmployeeID, Date, Status, LeaveID) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DB.getConnection();
            PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {

            // Check if attendance already exists for the given date
            checkStatement.setInt(1, Integer.parseInt(organizationID));
            checkStatement.setInt(2, Integer.parseInt(employeeID));
            checkStatement.setString(3, date);

            ResultSet rs = checkStatement.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return "Attendance already recorded for the given date.";
            }

            // If attendance is not recorded, proceed to insert the new record
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                insertStatement.setInt(1, Integer.parseInt(organizationID));
                insertStatement.setInt(2, Integer.parseInt(employeeID));
                insertStatement.setString(3, date);

                // Set status based on leaveID
                if (leaveID == null || leaveID.isEmpty()) {
                    insertStatement.setString(4, "present");
                    insertStatement.setNull(5, java.sql.Types.INTEGER); // leaveID is null
                } else {
                    insertStatement.setString(4, "absent");
                    insertStatement.setInt(5, Integer.parseInt(leaveID));
                }

                int rowsAffected = insertStatement.executeUpdate();
                if (rowsAffected > 0) {
                    return "Attendance successfully recorded.";
                } else {
                    return "Error recording attendance.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error occurred.";
        }
    }

    public static Map<String, Object> getEmployeeAttendance(String organizationID, String employeeID, String date) {
        String sql = "SELECT * FROM EmployeeAttendance WHERE OrganizationID = ? AND EmployeeID = ? AND Date = ?";
        Map<String, Object> attendanceDetails = new HashMap<>();

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));
            statement.setString(3, date);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                attendanceDetails.put("OrganizationID", rs.getInt("OrganizationID"));
                attendanceDetails.put("EmployeeID", rs.getInt("EmployeeID"));
                attendanceDetails.put("Date", rs.getString("Date"));
                attendanceDetails.put("Status", rs.getString("Status"));
                attendanceDetails.put("LeaveID", rs.getInt("LeaveID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return attendanceDetails;
    }

    public static boolean deleteEmployeeAttendance(String organizationID, String employeeID, String date) {
        String sql = "DELETE FROM EmployeeAttendance WHERE OrganizationID = ? AND EmployeeID = ? AND Date = ?";

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));
            statement.setString(3, date);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static String getManagerIDOfEmployeeDepartment(String organizationID, String employeeID) {
        String sql = "SELECT d.ManagerID " +
                    "FROM Departments d " +
                    "JOIN Employee e ON d.DepartmentID = e.DepartmentID " +
                    "WHERE e.OrganizationID = ? AND e.EmployeeID = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Return the ManagerID from the result set
                    return rs.getString("ManagerID");
                } else {
                    // No manager found
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

}