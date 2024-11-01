package DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class PayrollDAO {
    public static String createPayroll(String organizationID, String employeeID, String salary, String bonus, String deductions, String tax, String payDate, String description) {
        String getMaxPayrollIdSql = "SELECT MAX(PayrollID) FROM Payroll WHERE OrganizationID = ?";
        String insertSql = "INSERT INTO Payroll (PayrollID, OrganizationID, EmployeeID, Salary, Bonus, Deductions, Tax, PayDate, Description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DB.getConnection()) {
            // Step 1: Get the current maximum PayrollID for the organization
            int maxPayrollID = 1;
            try (PreparedStatement getMaxStmt = connection.prepareStatement(getMaxPayrollIdSql)) {
                getMaxStmt.setInt(1, Integer.parseInt(organizationID));
                ResultSet rs = getMaxStmt.executeQuery();

                if (rs.next()) {
                    maxPayrollID = rs.getInt(1); // Get the current max PayrollID
                }
            }

            // Step 2: Increment the PayrollID by 1 for the new payroll entry
            int newPayrollID = maxPayrollID + 1;

            // Step 3: Insert the new payroll entry with the incremented PayrollID
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, newPayrollID); // Set the new PayrollID
                insertStmt.setInt(2, Integer.parseInt(organizationID));
                insertStmt.setInt(3, Integer.parseInt(employeeID));
                insertStmt.setDouble(4, Double.parseDouble(salary));
                insertStmt.setDouble(5, Double.parseDouble(bonus));
                insertStmt.setDouble(6, Double.parseDouble(deductions));
                insertStmt.setDouble(7, Double.parseDouble(tax));
                insertStmt.setString(8, payDate);
                insertStmt.setString(9, description);

                int cnt = insertStmt.executeUpdate();
                return String.valueOf(newPayrollID);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    public static Map<String, Object> readPayroll(String payrollID, String organizationID) {
        String sql = "SELECT * FROM Payroll WHERE PayrollID = ? AND OrganizationID = ?";
        Map<String, Object> payroll = new HashMap<>();

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(payrollID));
            statement.setInt(2, Integer.parseInt(organizationID));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                payroll.put("PayrollID", resultSet.getInt("PayrollID"));
                payroll.put("OrganizationID", resultSet.getInt("OrganizationID"));
                payroll.put("EmployeeID", resultSet.getInt("EmployeeID"));
                payroll.put("Salary", resultSet.getDouble("Salary"));
                payroll.put("Bonus", resultSet.getDouble("Bonus"));
                payroll.put("Deductions", resultSet.getDouble("Deductions"));
                payroll.put("Tax", resultSet.getDouble("Tax"));
                payroll.put("PayDate", resultSet.getString("PayDate"));
                payroll.put("Description", resultSet.getString("Description"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payroll;
    }

    public static boolean updatePayroll(String organizationID, String payrollID, Map<String, Object> updates) {
        if (updates.isEmpty()) {
            return false; // No fields to update
        }

        StringBuilder sql = new StringBuilder("UPDATE Payroll SET ");
        Iterator<Map.Entry<String, Object>> iterator = updates.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            sql.append(entry.getKey()).append(" = ?");
            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        sql.append(" WHERE PayrollID = ? AND OrganizationID = ?");

        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                if(entry.getKey().equalsIgnoreCase("SALARY") || entry.getKey().equalsIgnoreCase("BONUS") || entry.getKey().equalsIgnoreCase("DEDUCTIONS") || entry.getKey().equalsIgnoreCase("TAX"))
                    statement.setObject(index++, Double.parseDouble(entry.getValue().toString()));
                else
                    statement.setObject(index++, entry.getValue());
            }
            statement.setInt(index++, Integer.parseInt(payrollID));
            statement.setInt(index, Integer.parseInt(organizationID));

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deletePayroll(String payrollID, String organizationID) {
        String sql = "DELETE FROM Payroll WHERE PayrollID = ? AND OrganizationID = ?";
        
        try (Connection connection = DB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Integer.parseInt(payrollID));
            statement.setInt(2, Integer.parseInt(organizationID));

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
