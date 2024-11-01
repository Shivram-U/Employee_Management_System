package BusinessLogic;

import DBUtil.*;
import Authentication.UserAuthentication;
import JSON.jsonHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class AssetMgmt extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String orgId = claims.get("organizationId");
        
        String empId = request.getParameter("empId");

        Map<String,Map<String, Object>> assets = AssetDAO.readAssignedAssets(orgId,empId);
        response.setStatus(HttpServletResponse.SC_OK);
        out.write(jsonHandler.convertMapToJson(assets));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String orgId = claims.get("organizationId");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);
        String empId = request.getParameter("EmployeeId");
        String assetId = jsonObject.getString("assetId");

        boolean success = AssetDAO.assignEmployee(orgId,empId, assetId);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Asset assigned successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to assign asset\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String orgId = claims.get("organizationId");

        JSONObject jsonObject = jsonHandler.parseJsonBody(request);
        String empId = request.getParameter("EmployeeId");
        String assetId = jsonObject.getString("assetId");

        boolean success = AssetDAO.unassignEmployee(orgId,empId,assetId);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Asset removed successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to remove asset\"}");
        }
    }
}
