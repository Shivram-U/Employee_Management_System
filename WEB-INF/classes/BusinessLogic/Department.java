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

public class Department extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        // Get department details
        String departmentId = request.getParameter("departmentId");
        Map<String, Object> department = DepartmentsDAO.readDepartment(orgId,departmentId);
        if (department != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write(jsonHandler.convertMapToJson(department)); // Convert map to JSON string or use a library for proper JSON conversion
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\": \"Department not found\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);

        String departmentName = jsonObject.getString("DepartmentName");
        String managerId = jsonObject.optString("ManagerId", null); // optional

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

   
        String deptId = DepartmentsDAO.createDepartment(orgId, departmentName, managerId);
        if (!deptId.equals("-1")) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.write("{\"DepartmentId\": \"" + deptId + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error creating department\"}");
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

        String departmentId = jsonObject.getString("DepartmentId");
        String managerId = jsonObject.optString("ManagerId",null);
        String departmentName = jsonObject.optString("DepartmentName",null);

        // Create a map for updated details
        Map<String, Object> updates = new HashMap<>();
        if (departmentName != null) updates.put("DepartmentName", departmentName);
        if (managerId != null) updates.put("ManagerId", managerId);

        // Update the department
        boolean success = DepartmentsDAO.updateDepartment(orgId,departmentId, updates);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Department updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to update department\"}");
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

        String departmentId = jsonObject.getString("departmentId");

        boolean success = DepartmentsDAO.deleteDepartment(orgId,departmentId);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"error\": \"Department deleted\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\": \"Department not found\"}");
        }
    }
}
