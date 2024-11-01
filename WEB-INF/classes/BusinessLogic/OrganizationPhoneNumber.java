package BusinessLogic;

import DBUtil.*; 
import Authentication.*;
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

public class OrganizationPhoneNumber extends HttpServlet {
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
            phoneNumbers = OrganizationsDAO.readPrimaryPhoneNumbers(orgId);
        }
        else
        {
            phoneNumbers = OrganizationsDAO.readPhoneNumbers(orgId);
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

        String phoneNumber = jsonObject.getString("PhoneNumber");
        boolean primary = jsonObject.getBoolean("Primary");

        boolean success = OrganizationsDAO.createPhoneNumber(orgId, phoneNumber, String.valueOf(primary));
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Phone number added successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error adding phone number\"}");
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

        String oldPhoneNumber = jsonObject.getString("OldPhoneNumber"); 
        String newPhoneNumber = jsonObject.optString("PhoneNumber", null);
        Boolean newPrimary = jsonObject.has("Primary") ? jsonObject.getBoolean("Primary") : null;

        boolean success = OrganizationsDAO.updatePhoneNumber(orgId, oldPhoneNumber, newPhoneNumber, newPrimary != null ? String.valueOf(newPrimary) : null);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Phone number updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to update phone number\"}");
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

        String phoneNumber = jsonObject.getString("PhoneNumber"); 
        if (phoneNumber != null) {
            boolean success = OrganizationsDAO.deletePhoneNumber(orgId, phoneNumber);
            if (success) {
                out.write("{\"message\": \"Phone number deleted\"}");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\": \"Phone number not found\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\": \"Phone number is required\"}");
        }
    }
}
