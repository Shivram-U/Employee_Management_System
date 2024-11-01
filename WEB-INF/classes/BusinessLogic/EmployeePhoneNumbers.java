package BusinessLogic;

import DBUtil.EmployeeDAO;
import Authentication.UserAuthentication;
import JSON.*;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class EmployeePhoneNumbers extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        String isPrimary = request.getParameter("Primary");
        Map<Integer, Object> phoneNumbers;
        if(isPrimary!=null && isPrimary.equals("true"))
        {
            phoneNumbers = EmployeeDAO.readPrimaryPhoneNumbers(orgId,empId);
        }
        else
        {
            phoneNumbers = EmployeeDAO.readPhoneNumbers(orgId,empId);
        }
        out.write(jsonHandler.convertMapToJson(phoneNumbers));
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

        boolean success = false;
        String phoneNumber = jsonObject.getString("phoneNumber");
        Boolean isPrimary = jsonObject.getBoolean("Primary");
        success = EmployeeDAO.createPhoneNumber(orgId, empId, phoneNumber, String.valueOf(isPrimary));


        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.write("{\"message\": \"Contact created successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to create contact\"}");
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

        boolean success = false;
        String oldPhoneNumber = jsonObject.getString("oldPhoneNumber");
        String newPhoneNumber = jsonObject.getString("newPhoneNumber");
        Boolean newPrimary = jsonObject.has("Primary") ? jsonObject.getBoolean("Primary") : null;

        success = EmployeeDAO.updatePhoneNumber(orgId, empId, oldPhoneNumber, newPhoneNumber, newPrimary != null ? String.valueOf(newPrimary) : null);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Contact updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to update contact\"}");
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

        String contactValue = jsonObject.getString("phoneNumber");
        
        boolean success = false;
        success = EmployeeDAO.deletePhoneNumber(orgId, empId, contactValue);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Contact deleted successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to delete contact\"}");
        }
    }
}
