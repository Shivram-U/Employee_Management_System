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

public class EmployeeLeave extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        String lrId = request.getParameter("LeaveRequestId");

        Map<String, Object> organization = AttendanceDAO.readLeaveRequest(orgId, empId, lrId);
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

        String leaveType = jsonObject.getString("LeaveType");
        String startDate = jsonObject.getString("StartDate");
        String endDate = jsonObject.getString("EndDate");
        String description = jsonObject.getString("Description");
 
        String message = AttendanceDAO.createLeaveRequest(orgId,empId,leaveType,startDate,endDate,description);

        response.setStatus(HttpServletResponse.SC_OK);
        out.write("{\"Message\": \"" + message + "\"}");
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

        String lrId = jsonObject.getString("LeaveRequestId");
        String leaveType = jsonObject.getString("LeaveType");
        String description = jsonObject.getString("Description");
 
        Map<String, Object> updates = new HashMap<>();
        if (leaveType!=null) 
            updates.put("LeaveType", leaveType);
        if (description!=null) 
            updates.put("Description", description);

        String message = AttendanceDAO.updateLeaveRequest(orgId, lrId, updates);
        response.setStatus(HttpServletResponse.SC_OK);
        out.write("{\"message\": \""+message+"\"}");
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

        String lrId = jsonObject.getString("LeaveRequestId");

        String message = AttendanceDAO.deleteLeaveRequest(orgId,lrId);
        response.setStatus(HttpServletResponse.SC_OK);
        out.write("{\"message\": \""+message+"\"}");
    }
}
