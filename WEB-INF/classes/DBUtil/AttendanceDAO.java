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
import java.sql.Date;
import java.util.Calendar;


public class AttendanceDAO {
    public static boolean isAttendanceRecorded(String organizationID, String employeeID, String date) {
        String checkSql = "SELECT 1 FROM EmployeeAttendance WHERE OrganizationID = ? AND EmployeeID = ? AND Date = ?";

        try (Connection connection = DB.getConnection();
            PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {

            // Set parameters for the query
            checkStatement.setInt(1, Integer.parseInt(organizationID));
            checkStatement.setInt(2, Integer.parseInt(employeeID));

            // Convert the input date (String) to java.sql.Date
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            checkStatement.setDate(3, sqlDate);

            ResultSet resultSet = checkStatement.executeQuery();

            // If a result is found, it means attendance has already been recorded
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Return false in case of error (no attendance found)
        }
    }

    public static String createEmployeeAttendance(String organizationID, String employeeID, String date, String leaveID) {
        // SQL to check if a record already exists for the given organizationID, employeeID, and date
        String checkSql = "SELECT COUNT(*) FROM EmployeeAttendance WHERE OrganizationID = ? AND EmployeeID = ? AND Date = ?";
        String insertSql = "INSERT INTO EmployeeAttendance (OrganizationID, EmployeeID, Date, Status, LeaveID) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            
            // Check if attendance already exists for the same organization, employee, and date
            checkStatement.setInt(1, Integer.parseInt(organizationID));
            checkStatement.setInt(2, Integer.parseInt(employeeID));
            checkStatement.setDate(3, java.sql.Date.valueOf(date));

            ResultSet rs = checkStatement.executeQuery();
            rs.next(); // Move the cursor to the first row
            int count = rs.getInt(1); // Get the count of matching records

            if (count > 0) {
                // If a record exists, do not allow insertion and return false
                return "Attendance already recorded";
            }

            // If no record exists, proceed with the insertion
            insertStatement.setInt(1, Integer.parseInt(organizationID));
            insertStatement.setInt(2, Integer.parseInt(employeeID));
            insertStatement.setDate(3, java.sql.Date.valueOf(date));

            // Set status based on leaveID
            if (leaveID == null) {
                insertStatement.setString(4, "present");
                insertStatement.setNull(5, java.sql.Types.INTEGER); // leaveID is null
            } 
            else if (leaveID.equals("-1")) {
                insertStatement.setString(4, "holiday");
                insertStatement.setNull(5, java.sql.Types.INTEGER); // leaveID is null
            } 
            else {
                insertStatement.setString(4, "absent");
                insertStatement.setInt(5, Integer.parseInt(leaveID));
            }

            // Execute the insert statement
            int rowsAffected = insertStatement.executeUpdate();
            return "Attendance recorded";  // Return true if the insertion is successful

        } catch (SQLException e) {
            e.printStackTrace();
            return "Server Error";  // Return false in case of error
        }
    }


    public static Map<String, Object> getEmployeeAttendance(String organizationID, String employeeID, String date) {
        String sql = "SELECT * FROM EmployeeAttendance WHERE OrganizationID = ? AND EmployeeID = ? AND Date = ?";
        Map<String, Object> attendanceDetails = new HashMap<>();

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set OrganizationID and EmployeeID
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));

            // Convert the input date (String) to java.sql.Date
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            statement.setDate(3, sqlDate);

            ResultSet rs = statement.executeQuery();

            // Fetch result if available
            if (rs.next()) {
                attendanceDetails.put("OrganizationID", rs.getInt("OrganizationID"));
                attendanceDetails.put("EmployeeID", rs.getInt("EmployeeID"));
                attendanceDetails.put("Date", rs.getDate("Date").toString());  // Get as java.sql.Date
                attendanceDetails.put("Status", rs.getString("Status"));
                
                // Handle possible null LeaveID
                int leaveID = rs.getInt("LeaveID");
                if (rs.wasNull()) {
                    attendanceDetails.put("LeaveID", null);
                } else {
                    attendanceDetails.put("LeaveID", leaveID);
                }
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

            // Set OrganizationID and EmployeeID
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));

            // Convert the input date (String) to java.sql.Date
            java.sql.Date sqlDate = java.sql.Date.valueOf(date);
            statement.setDate(3, sqlDate);

            return statement.executeUpdate() > 0; // Return true if the deletion is successful

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean isHolidayAlreadyIssued(String organizationID, String holidayDate) {
        String selectSql = "SELECT * FROM Holiday WHERE OrganizationID = ? AND HolidayDate = ?";

        try (Connection connection = DB.getConnection();
            PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

            // Set the parameters for organization ID and holiday date
            selectStatement.setInt(1, Integer.parseInt(organizationID));
            selectStatement.setString(2, holidayDate);

            // Execute the query
            ResultSet resultSet = selectStatement.executeQuery();

            // Check if the result set contains any data, i.e., the holiday exists
            if (resultSet.next()) {
                return true;  // Holiday already issued
            } else {
                return false; // No holiday found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // In case of an error, consider it as no holiday found
        }
    }

    public static boolean markHolidayForAllEmployeesInOrganization(String organizationID, String date) {
        // SQL to get all employees of the organization
        String selectSql = "SELECT EmployeeID FROM Employee WHERE OrganizationID = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
            
            // Set the organizationID parameter
            selectStatement.setInt(1, Integer.parseInt(organizationID));
            
            // Execute the query to get all employees of the organization
            ResultSet rs = selectStatement.executeQuery();
            
            // Loop through each employee and create an attendance record with 'Holiday' status
            while (rs.next()) {
                String employeeID = rs.getString("EmployeeID");
                
                // Call the createEmployeeAttendance function for each employee, marking as 'Holiday'
                createEmployeeAttendance(organizationID, employeeID, date, "-1");
            }

            // If all records were successfully created, return true
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean invalidatePendingLeavesForHoliday(String organizationID, String holidayDate) {
        // SQL query to update the status of leave requests that are pending and overlap with the holiday date
        String sql = "UPDATE `Leave` " +
                    "SET Status = 'invalid' " +
                    "WHERE OrganizationID = ? " +
                    "AND Status = 'pending' " +
                    "AND ? BETWEEN StartDate AND EndDate";

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Set the organizationID and holidayDate in the prepared statement
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setDate(2, java.sql.Date.valueOf(holidayDate));

            // Execute the update query
            int rowsAffected = statement.executeUpdate();
            
            // Return true if at least one row was affected (i.e., some leave requests were invalidated)
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Return false in case of error
        }
    }


    public static Boolean createHoliday(String organizationID, String holidayDate, String description) {
        String insertSql = "INSERT INTO Holiday (OrganizationID, HolidayDate, Description) VALUES (?, ?, ?)";
        
        // Holiday does not exist, so create a new holiday
        try (Connection connection = DB.getConnection();
                PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            insertStatement.setInt(1, Integer.parseInt(organizationID));
            insertStatement.setString(2, holidayDate);
            insertStatement.setString(3, description);
            
            int rowsInserted = insertStatement.executeUpdate();
            invalidatePendingLeavesForHoliday(organizationID,holidayDate);
            if (rowsInserted > 0) {
                return true;
            } else {
                return false;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Map<String,Map<String, Object>> readHolidays(String organizationID) {
        String sql = "SELECT * FROM Holiday WHERE OrganizationID = ?";
        Map<String,Map<String, Object>> holidays = new HashMap<>();

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, Integer.parseInt(organizationID));

            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Map<String, Object> holiday = new HashMap<>();
                holiday.put("OrganizationID", resultSet.getInt("OrganizationID"));
                holiday.put("HolidayDate", resultSet.getString("HolidayDate"));
                holiday.put("Description", resultSet.getString("Description"));
                holidays.put(resultSet.getString("HolidayDate"),holiday);
            }
            
            return holidays;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Map<String, Object> readHoliday(String organizationID, String holidayDate) {
        String sql = "SELECT * FROM Holiday WHERE OrganizationID = ? AND HolidayDate = ?";
        Map<String, Object> holiday = new HashMap<>();

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setString(2, holidayDate);

            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                holiday.put("OrganizationID", resultSet.getInt("OrganizationID"));
                holiday.put("HolidayDate", resultSet.getString("HolidayDate"));
                holiday.put("Description", resultSet.getString("Description"));
            }
            
            return holiday;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static boolean updateHoliday(String organizationID, String holidayDate, String description) {
        String sql = "UPDATE Holiday SET Description = ? WHERE OrganizationID = ? AND HolidayDate = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, description);
            statement.setInt(2, Integer.parseInt(organizationID));
            statement.setString(3, holidayDate);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteHoliday(String organizationID, String holidayDate) {
        String sql = "DELETE FROM Holiday WHERE OrganizationID = ? AND HolidayDate = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setString(2, holidayDate);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLeaveOverlapping(String organizationID, String employeeID, String startDate, String endDate) {
        String checkSql = "SELECT LeaveRequestID FROM `Leave` WHERE OrganizationID = ? AND EmployeeID = ? AND Status IN ('Approved', 'Pending') " +
                        "AND ((StartDate <= ? AND EndDate >= ?) OR (StartDate <= ? AND EndDate >= ?) OR (StartDate >= ? AND EndDate <= ?))";

        try (Connection connection = DB.getConnection();
            PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {

            // Set parameters for the collision check query
            checkStatement.setInt(1, Integer.parseInt(organizationID));
            checkStatement.setInt(2, Integer.parseInt(employeeID));
            checkStatement.setString(3, startDate); // Compare with current startDate and endDate
            checkStatement.setString(4, startDate);
            checkStatement.setString(5, endDate);
            checkStatement.setString(6, endDate);
            checkStatement.setString(7, startDate);
            checkStatement.setString(8, endDate);

            ResultSet resultSet = checkStatement.executeQuery();
            // If a result is found, it means there is an overlapping leave request
            return resultSet.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return true;  // In case of error, we can assume there's an issue and avoid creating a new request.
        }
    }

    public static boolean isLeaveRequestOverlappingWithApprovedLeaves(String organizationID, String employeeID, String startDate, String endDate) {
        String sql = "SELECT 1 FROM `Leave` WHERE OrganizationID = ? AND EmployeeID = ? " +
                    "AND Status = 'approved' " + 
                    "AND (StartDate <= ? AND EndDate >= ?)";

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters for organizationID, employeeID, startDate, and endDate
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));
            statement.setDate(3, java.sql.Date.valueOf(endDate));  // EndDate of the past approved leaves
            statement.setDate(4, java.sql.Date.valueOf(startDate)); // StartDate of the past approved leaves

            ResultSet rs = statement.executeQuery();
            // If we find any result, it means there is an overlap
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isLeaveRequestOverlappingWithHolidays(String organizationID, String startDate, String endDate) {
        String sql = "SELECT 1 FROM Holiday WHERE OrganizationID = ? " +
                    "AND (HolidayDate BETWEEN ? AND ?)";

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters for organizationID, startDate, and endDate
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setDate(2, java.sql.Date.valueOf(startDate));  // Requested startDate
            statement.setDate(3, java.sql.Date.valueOf(endDate));    // Requested endDate

            ResultSet rs = statement.executeQuery();
            // If we find any result, it means there is an overlap with the holiday
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String createLeaveRequest(String organizationID, String employeeID, String leaveType, String startDate, String endDate, String description) {
        String maxIdSql = "SELECT MAX(LeaveRequestID) AS maxLeaveRequestID FROM `Leave` WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO `Leave` (OrganizationID, LeaveRequestID, EmployeeID, LeaveType, StartDate, EndDate, Description, Status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        if(isLeaveRequestOverlappingWithApprovedLeaves(organizationID,employeeID,startDate,endDate))
            return "Leave request dates are overlapping with your already issued leaves.";
        if(isLeaveRequestOverlappingWithHolidays(organizationID,startDate,endDate))
            return "Leave request dates are overlapping with your organization's already issued holidays.";

        try (Connection connection = DB.getConnection();
            PreparedStatement maxIdStatement = connection.prepareStatement(maxIdSql);
            PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

            // Get the maximum LeaveRequestID for the given OrganizationID
            maxIdStatement.setInt(1, Integer.parseInt(organizationID));
            ResultSet rs = maxIdStatement.executeQuery();
            int newLeaveRequestID = 1;  // Default value if no leave requests exist

            // If there's already a leave request, increment the max LeaveRequestID
            if (rs.next() && rs.getInt("maxLeaveRequestID") > 0) {
                newLeaveRequestID = rs.getInt("maxLeaveRequestID") + 1;
            }

            // Now proceed with the insertion of the new leave request
            insertStatement.setInt(1, Integer.parseInt(organizationID));
            insertStatement.setInt(2, newLeaveRequestID);  // Set the new LeaveRequestID
            insertStatement.setInt(3, Integer.parseInt(employeeID));
            insertStatement.setString(4, leaveType);
            insertStatement.setDate(5, java.sql.Date.valueOf(startDate));
            insertStatement.setDate(6, java.sql.Date.valueOf(endDate));
            insertStatement.setString(7, description);
            insertStatement.setString(8, "pending");

            // Execute the insert statement and return the result
            insertStatement.executeUpdate();
            return String.valueOf("Leave Request issued : "+newLeaveRequestID);

        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }


    public static Map<String, Object> readLeaveRequest(String organizationID,String employeeId, String leaveRequestID) {
        String sql = "SELECT * FROM `Leave` WHERE OrganizationID = ? AND employeeID = ? AND LeaveRequestID = ?";
        Map<String, Object> leaveDetails = new HashMap<>();

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeId));
            statement.setInt(3, Integer.parseInt(leaveRequestID));

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                leaveDetails.put("OrganizationID", resultSet.getInt("OrganizationID"));
                leaveDetails.put("LeaveRequestID", resultSet.getInt("LeaveRequestID"));
                leaveDetails.put("EmployeeID", resultSet.getInt("EmployeeID"));
                leaveDetails.put("LeaveType", resultSet.getString("LeaveType"));
                leaveDetails.put("StartDate", resultSet.getDate("StartDate").toString());
                leaveDetails.put("EndDate", resultSet.getDate("EndDate").toString());
                leaveDetails.put("Description", resultSet.getString("Description"));
                leaveDetails.put("Status", resultSet.getString("Status"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaveDetails;
    }

    public static String updateLeaveRequest(String organizationID, String leaveRequestID, Map<String, Object> updateFields) {
        // Step 1: Check if the leave request is approved
        String statusCheckSql = "SELECT Status FROM `Leave` WHERE OrganizationID = ? AND LeaveRequestID = ?";
        StringBuilder sql = new StringBuilder("UPDATE `Leave` SET ");

        for (String field : updateFields.keySet()) {
            sql.append(field).append(" = ?, ");
        }

        sql.setLength(sql.length() - 2);  // Remove the trailing comma
        sql.append(" WHERE OrganizationID = ? AND LeaveRequestID = ?");

        try (Connection connection = DB.getConnection();
            PreparedStatement statusCheckStatement = connection.prepareStatement(statusCheckSql);
            PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            // Check current status
            statusCheckStatement.setInt(1, Integer.parseInt(organizationID));
            statusCheckStatement.setInt(2, Integer.parseInt(leaveRequestID));
            ResultSet statusResult = statusCheckStatement.executeQuery();


            if (statusResult.next())
            {
                String status = statusResult.getString("Status");
                if("approved".equalsIgnoreCase(status)) 
                    return "Leave request is already approved, hence cannot be updated";
                else if("rejected".equalsIgnoreCase(status)) 
                    return "Leave request is already rejected, hence cannot be updated";
                else if("invalid".equalsIgnoreCase(status)) 
                    return "Leave request is already invalidated, hence cannot be updated";
            }
            else
                return "Leave request does not exist";

            // Step 2: Proceed with the update
            int index = 1;
            for (String field : updateFields.keySet()) {
                statement.setString(index++, updateFields.get(field).toString());
            }

            statement.setInt(index++, Integer.parseInt(organizationID));
            statement.setInt(index, Integer.parseInt(leaveRequestID));

            statement.executeUpdate();

            return "Leave request updated";

        } catch (SQLException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public static String deleteLeaveRequest(String organizationID, String leaveRequestID) {
        // Step 1: Check if the leave request is approved
        String statusCheckSql = "SELECT Status FROM `Leave` WHERE OrganizationID = ? AND LeaveRequestID = ?";
        String sql = "DELETE FROM `Leave` WHERE OrganizationID = ? AND LeaveRequestID = ?";

        try (Connection connection = DB.getConnection();
            PreparedStatement statusCheckStatement = connection.prepareStatement(statusCheckSql);
            PreparedStatement statement = connection.prepareStatement(sql)) {

            // Check current status
            statusCheckStatement.setInt(1, Integer.parseInt(organizationID));
            statusCheckStatement.setInt(2, Integer.parseInt(leaveRequestID));
            ResultSet statusResult = statusCheckStatement.executeQuery();

            if (statusResult.next())
            {
                if("approved".equalsIgnoreCase(statusResult.getString("Status"))) 
                    return "Leave request is already approved, hence cannot be deleted";
            }
            else
                return "Leave request does not exist";


            // Step 2: Proceed with the delete
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(leaveRequestID));

            statement.executeUpdate();  // Return true if the deletion is successful
            return "Leave Request is deleted";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Server Error";
        }
    }

    public static boolean invalidateOverlappingPendingLeaveRequests(String organizationID, String employeeID, String approvedStartDate, String approvedEndDate) {
        String sql = "UPDATE `Leave` " +
                    "SET Status = 'invalid' " +
                    "WHERE OrganizationID = ? AND EmployeeID = ? AND Status = 'pending' " +
                    "AND (StartDate <= ? AND EndDate >= ?)";

        try (Connection connection = DB.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters for organizationID, employeeID, approvedStartDate, and approvedEndDate
            statement.setInt(1, Integer.parseInt(organizationID));
            statement.setInt(2, Integer.parseInt(employeeID));
            statement.setDate(3, java.sql.Date.valueOf(approvedEndDate));   // Approved leave request's end date
            statement.setDate(4, java.sql.Date.valueOf(approvedStartDate)); // Approved leave request's start date

            // Execute the update query
            int rowsAffected = statement.executeUpdate();

            // Return true if at least one row was updated
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String updateLeaveRequestStatus(String organizationID, String leaveRequestID, String employeeID, String status) {
        // Add an additional condition to ensure the status is 'pending' before updating
        String updateSql = "UPDATE `Leave` SET Status = ? WHERE OrganizationID = ? AND LeaveRequestID = ? ";
        
        // Query to get leave details (EmployeeID, StartDate, EndDate) before updating the status
        String selectSql = "SELECT status, StartDate, EndDate FROM `Leave` WHERE OrganizationID = ? AND LeaveRequestID = ?";
        
        try (Connection connection = DB.getConnection();
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

            // Set the IDs to fetch leave details
            selectStatement.setInt(1, Integer.parseInt(organizationID));
            selectStatement.setInt(2, Integer.parseInt(leaveRequestID));
            
            ResultSet rs = selectStatement.executeQuery();

            if (rs.next()) {
                // Fetch the employeeID, startDate, and endDate from the result set
                String startDate = rs.getString("StartDate");
                String endDate = rs.getString("EndDate");
                String currentStatus = rs.getString("status");
                
                if(currentStatus.equals("pending"))
                {
                    // Set the new status and IDs for the update query
                    updateStatement.setString(1, status);
                    updateStatement.setInt(2, Integer.parseInt(organizationID));
                    updateStatement.setInt(3, Integer.parseInt(leaveRequestID));

                    // Execute the update and check if at least one row is affected
                    boolean isUpdated = updateStatement.executeUpdate() > 0;

                    if (isUpdated)
                    {
                        if(status.equalsIgnoreCase("approved")) {
                        // Call markAttendanceForLeave only if the status is 'approved'
                            markAttendanceForLeave(organizationID, employeeID, startDate, endDate, leaveRequestID);
                            invalidateOverlappingPendingLeaveRequests(organizationID, employeeID, startDate, endDate);
                            return "Leave approved";
                        }
                        else
                        {
                            return "Leave rejected";
                        }
                    }
                }
                else if(currentStatus.equals("Invalid"))
                {
                    return "Leave request is invalidated";
                }
                else if(currentStatus.equals("approved"))
                {
                    return "Leave request already accepted";
                }
                return "Leave request already rejected";
            }
            return "Leave request does not exist";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Server Error";
        }
    }


    public static boolean markAttendanceForLeave(String organizationID, String employeeID, String startDate, String endDate, String leaveID) {
        try {
            // Parse the startDate and endDate to java.sql.Date
            java.sql.Date start = java.sql.Date.valueOf(startDate);  // Convert to SQL date
            java.sql.Date end = java.sql.Date.valueOf(endDate);      // Convert to SQL date

            // Set up calendar to iterate over the date range
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);

            while (!calendar.getTime().after(end)) {
                String currentDate = new java.sql.Date(calendar.getTimeInMillis()).toString();  // Format as YYYY-MM-DD

                // Call the createEmployeeAttendance function for each date in the range
                createEmployeeAttendance(organizationID, employeeID, currentDate, leaveID);

                // Move to the next day
                calendar.add(Calendar.DATE, 1);
            }

            return true;  // Return true if attendance for all days is successfully marked

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}