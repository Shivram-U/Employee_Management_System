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

public class Holidays extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");
        
        JSONObject jsonObject = jsonHandler.parseJsonBody(request);

        String date = jsonObject.getString("date");
        if(date == null)
        {
            Map<String,Map<String, Object>> holidays = AttendanceDAO.readHolidays(orgId);
            out.write(jsonHandler.convertMapToJson(holidays)); // Convert map to JSON string or use a library for proper JSON conversion
        }
        else
        {
            Map<String, Object> holiday = AttendanceDAO.readHoliday(orgId,date);
            out.write(jsonHandler.convertMapToJson(holiday)); // Convert map to JSON string or use a library for proper JSON conversion
        }
        response.setStatus(HttpServletResponse.SC_OK);
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

        String date = jsonObject.getString("date");
        String description = jsonObject.getString("description");
        if(AttendanceDAO.isHolidayAlreadyIssued(orgId,date))
        {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"error\": \"Holiday already recorded\"}");
        }
        else
        {
            Boolean status = AttendanceDAO.createHoliday(orgId,date,description);
            if(status)
            {
                AttendanceDAO.markHolidayForAllEmployeesInOrganization(orgId,date);
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\": \"Holiday Issued\"}");
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Holiday not recorded\"}");
            }
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

        String date = jsonObject.getString("date");
        
        String description = jsonObject.getString("description");
        if(AttendanceDAO.isHolidayAlreadyIssued(orgId,date))
        {
            Boolean status = AttendanceDAO.updateHoliday(orgId,date,description);
            if(status)
            {
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"message\": \"Holiday updated\"}");
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Holiday not updated\"}");
            }
        }
        else
        {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"error\": \"Holiday does not exist\"}");
        }
    }
}
