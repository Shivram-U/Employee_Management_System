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

public class EmployeeLeaveMgmt extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        String lrId = request.getParameter("LeaveRequestId");
        String employeeId = request.getParameter("EmployeeId");

        Map<String, Object> organization = AttendanceDAO.readLeaveRequest(orgId,employeeId, lrId);
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

        String employeeID = request.getParameter("EmployeeId");
        String lrId = jsonObject.getString("LeaveRequestId");

        String isUpdated = AttendanceDAO.updateLeaveRequestStatus(orgId, lrId, employeeID, "approved");
        response.setStatus(HttpServletResponse.SC_OK);
        out.write("{\"message\": \""+isUpdated+"\"}");
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

        String employeeID = request.getParameter("EmployeeId");
        String lrId = jsonObject.getString("LeaveRequestId");

        String isUpdated = AttendanceDAO.updateLeaveRequestStatus(orgId, lrId, employeeID, "rejected");

        response.setStatus(HttpServletResponse.SC_OK);
        out.write("{\"message\": \""+isUpdated+"\"}");
    }
}
