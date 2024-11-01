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

public class Organization extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        Map<String, Object> organization = OrganizationsDAO.readOrganization(orgId);
        response.setStatus(HttpServletResponse.SC_OK);
        out.write(jsonHandler.convertMapToJson(organization)); // Convert map to JSON string or use a library for proper JSON conversion

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);

        String OrgName = jsonObject.getString("OrgName");
        String OrgAddress = jsonObject.getString("OrgAddress");
        String startDate = jsonObject.getString("StartDate");

        String orgId = OrganizationsDAO.createOrganization(OrgName, OrgAddress, startDate);

        if (!orgId.equals("-1")) {
            // Extract parameters for creating a CEO employee
            String Name = jsonObject.getString("Name");
            String dob = jsonObject.getString("DOB");
            String emailId = jsonObject.getString("emailId");
            String doorNumber = jsonObject.getString("DoorNumber");
            String area = jsonObject.getString("Area");
            String city = jsonObject.getString("City");
            String district = jsonObject.getString("District");
            String country = jsonObject.getString("Country");
            String pincode = jsonObject.getString("Pincode");
            String joiningDate = jsonObject.getString("JoiningDate");
            String officeLocation = jsonObject.getString("OfficeLocation");
            String managerId = null; // Initially, set to null
            String employmentType = jsonObject.getString("EmploymentType");
            String maritalStatus = jsonObject.getString("MaritalStatus");
            String password = jsonObject.getString("Password");

            String deptId = DepartmentsDAO.createDepartment(orgId, "Administration", managerId);
            String roleId = RolesDAO.createRole(orgId, "CEO");

            String empId = EmployeeDAO.createEmployee(orgId, emailId, Name, dob, doorNumber, area, city, district, country, pincode, deptId, roleId, joiningDate, officeLocation, managerId, employmentType, maritalStatus, password);

            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"OrganizationId\": \"" + orgId + "\", \"EmployeeId\": \"" + empId + "\"}");

        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error creating organization\"}");
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
        String name = jsonObject.optString("OrgName");
        String address = jsonObject.optString("OrgAddress");
        String startDate = jsonObject.optString("StartDate");
    
        Map<String, Object> updates = new HashMap<>();
        if (!name.isEmpty()) updates.put("Name", name);
        if (!address.isEmpty()) updates.put("Address", address);
        if (!startDate.isEmpty()) updates.put("StartDate", startDate);
    

        boolean success = OrganizationsDAO.updateOrganization(orgId, updates);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Organization updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to update organization\"}");
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

        boolean success = OrganizationsDAO.deleteOrganization(orgId);
        if (success) {
            out.write("{\"Message\" : \"Organization deleted successfully\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            out.write("{\"Message\" : \"Organization not deleted\"}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
