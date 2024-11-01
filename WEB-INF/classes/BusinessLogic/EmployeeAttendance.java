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

public class EmployeeAttendance extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");
        
        String employeeId = request.getParameter("EmployeeId");
        String date = request.getParameter("date");
        Map<String, Object> organization = AttendanceDAO.getEmployeeAttendance(orgId,employeeId,date);
        response.setStatus(HttpServletResponse.SC_OK);
        out.write(jsonHandler.convertMapToJson(organization)); // Convert map to JSON string or use a library for proper JSON conversion
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

        String employeeId = request.getParameter("EmployeeId");
        String date = jsonObject.getString("date");
        String message = AttendanceDAO.createEmployeeAttendance(orgId,employeeId,date,null);

        response.setStatus(HttpServletResponse.SC_OK);
        out.write("{\"message\": \""+message+"\"}");
    }

    /*
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);
        String date = jsonObject.getString("date");
        String employeeId = request.getParameter("EmployeeId");

        Boolean success = AttendanceDAO.deleteEmployeeAttendance(orgId,employeeId,date);

        if (success) {
            out.write("{\"Message\" : \"Attendance deleted successfully\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            out.write("{\"Message\" : \"Attendance not deleted\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }*/
}
