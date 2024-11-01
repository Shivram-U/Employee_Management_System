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

public class Assets extends HttpServlet {
    private AssetDAO AssetDAO = new AssetDAO();
    private EmployeeDAO EmployeeDAO = new EmployeeDAO();
    private UserAuthentication UserAuthentication = new UserAuthentication();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    
        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");

        String assetId = request.getParameter("assetId");
        Map<String, Object> asset = AssetDAO.readAsset(orgId, assetId);

        if (asset != null && !asset.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write(jsonHandler.convertMapToJson(asset));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\": \"Asset not found\"}");
        }
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

        String name = jsonObject.getString("Name");
        String price = jsonObject.getString("Price");
        String purchaseDate = jsonObject.getString("PurchaseDate");
        String status = jsonObject.getString("Status");

        String newAssetId = AssetDAO.createAsset(orgId, name, price, purchaseDate, status);

        if (!newAssetId.equals("-1")) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"AssetId\": \"" + newAssetId + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error creating asset\"}");
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

        String assetId = jsonObject.getString("AssetId");

        Map<String, Object> updates = new HashMap<>();
        String name = jsonObject.optString("Name", null);
        String price = jsonObject.optString("Price",null);
        String purchaseDate = jsonObject.optString("PurchaseDate",null);
        String status = jsonObject.optString("Status",null);
        if(name!=null)
            updates.put("Name", name);
        if(price!=null)
            updates.put("Price", price);
        if(purchaseDate!=null)
            updates.put("PurchaseDate", purchaseDate);
        if(status!=null)
            updates.put("Status", status);

        boolean success = AssetDAO.updateAsset(orgId, assetId, updates);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Asset updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to update asset\"}");
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

        String assetId = jsonObject.getString("assetId");

        boolean success = AssetDAO.deleteAsset(orgId, assetId);

        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Asset deleted successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Failed to delete asset\"}");
        }
    }
}
