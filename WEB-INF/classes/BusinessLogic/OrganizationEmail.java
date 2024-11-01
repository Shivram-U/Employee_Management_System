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

public class OrganizationEmail extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");
        String emailId = request.getParameter("EmailId");
        if(emailId == null)
        {
            Map<Integer, Object> emails = OrganizationsDAO.readOrganizationEmails(orgId);
            out.write(jsonHandler.convertMapToJson(emails)); // Convert map to JSON string or use a library for proper JSON conversion
            }
        else
        {
            Map<String, Object> emails = OrganizationsDAO.readOrganizationEmail(orgId,emailId);
            out.write(jsonHandler.convertMapToJson(emails)); // Convert map to JSON string or use a library for proper JSON conversion
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
        String email = jsonObject.optString("Email");
        boolean success = OrganizationsDAO.createOrganizationEmail(orgId, email);
        if (success) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.write("{\"message\": \"Email added successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error adding email\"}");
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
        String email = jsonObject.getString("Email");
        String emailId = jsonObject.getString("EmailId");

        boolean success = OrganizationsDAO.updateOrganizationEmail(orgId, emailId, email);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Email updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error updating email\"}");
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
        String emailId = jsonObject.getString("EmailId");

        boolean success = OrganizationsDAO.deleteOrganizationEmail(orgId, emailId);
        if (success) {
            out.write("{\"message\": \"Email deleted\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\": \"Email not found\"}");
        }
    }
}
