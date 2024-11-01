package BusinessLogic;

import DBUtil.*;
import Authentication.UserAuthentication;
import JSON.*;

import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject; 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Employee extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Read operation
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        try {
            Map<String, Object> employeeData = EmployeeDAO.readEmployee(orgId,empId);
            if (employeeData != null) {
                response.setContentType("application/json");
                out.write(jsonHandler.convertMapToJson(employeeData));
            } else {
                out.write("{\"error\": \"Employee not found\"}");
            }
        } catch (Exception e) {
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
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

        JSONObject json = jsonHandler.parseJsonBody(request);
        
        Map<String, Object> updates = new HashMap<>();
        if (json.has("Name")) 
            updates.put("Name", json.getString("Name"));
        if (json.has("DOB")) 
            updates.put("dob", json.getString("DOB"));
        if (json.has("emailId")) 
            updates.put("EmailId", json.getString("emailId"));
        if (json.has("DoorNumber")) 
            updates.put("doorNumber", json.getString("DoorNumber"));
        if (json.has("Area"))   
            updates.put("area", json.getString("Area"));
        if (json.has("City")) 
            updates.put("city", json.getString("City"));
        if (json.has("District")) 
            updates.put("district", json.getString("District"));
        if (json.has("Country"))   
            updates.put("country", json.getString("Country"));
        if (json.has("Pincode")) 
            updates.put("pincode", json.getString("Pincode"));
        if (json.has("JoiningDate")) 
            updates.put("joiningDate", json.getString("JoiningDate"));
        if (json.has("OfficeLocation")) 
            updates.put("officeLocation", json.getString("OfficeLocation"));
        if (json.has("EmploymentType")) 
            updates.put("employmentType", json.getString("EmploymentType"));
        if (json.has("MaritalStatus")) 
            updates.put("maritalStatus", json.getString("MaritalStatus"));

        try {
            boolean success = EmployeeDAO.updateEmployee(orgId, empId, updates);
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\": \"Employee updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Failed to update employee\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
