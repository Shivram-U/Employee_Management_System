package BusinessLogic;

import DBUtil.*;

import Authentication.UserAuthentication;
import JSON.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Payroll extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        String payrollId = request.getParameter("payrollId");

        Map<String, Object> payroll = PayrollDAO.readPayroll(payrollId,orgId);
        response.setStatus(HttpServletResponse.SC_OK);
        out.write(jsonHandler.convertMapToJson(payroll)); // Convert map to JSON string or use a library for proper JSON conversion
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);

        String employeeId = jsonObject.getString("EmployeeId");
        String salary = jsonObject.getString("Salary");
        String bonus = jsonObject.getString("Bonus");
        String deductions = jsonObject.getString("Deductions");
        String tax = jsonObject.getString("Tax");
        String paydate = jsonObject.getString("PayDate");
        String description = jsonObject.getString("Description");

        String payrollId = PayrollDAO.createPayroll(orgId, employeeId, salary, bonus, deductions, tax, paydate, description);

        if(!payrollId.equals("-1")) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"PayrollId\": \"" + payrollId + "\"}");

        } 
        else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error creating payroll\"}");
        }
    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    
        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);
        String payrollId = jsonObject.getString("payrollId");
        String employeeId = jsonObject.getString("EmployeeId");
        String salary = jsonObject.getString("Salary");
        String bonus = jsonObject.getString("Bonus");
        String deductions = jsonObject.getString("Deductions");
        String tax = jsonObject.getString("Tax");
        String paydate = jsonObject.getString("PayDate");
        String description = jsonObject.getString("Description");

        Map<String, Object> updates = new HashMap<>();
        if (employeeId!=null) 
            updates.put("EmployeeId", employeeId);
        if (salary!=null) 
            updates.put("Salary", salary);
        if (bonus!=null) 
            updates.put("Bonus", bonus);
        if (deductions!=null) 
            updates.put("Deductions", deductions);
        if (tax!=null)
            updates.put("Tax", tax);
        if (paydate!=null) 
            updates.put("paydate", paydate);
        if (description!=null) 
            updates.put("Description", description);
      

        boolean success = PayrollDAO.updatePayroll(orgId, payrollId, updates);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Payroll updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to update payroll\"}");
        }
    }
    

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);
        String payrollId = jsonObject.getString("payrollId");

        boolean success = PayrollDAO.deletePayroll(payrollId,orgId);
        if (success) {
            out.write("{\"Message\" : \"Payroll deleted successfully\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            out.write("{\"error\" : \"Payroll not deleted\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
