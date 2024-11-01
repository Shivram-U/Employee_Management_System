package BusinessLogic;

import DBUtil.*;
import JSON.*;
import Authentication.UserAuthentication;

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

public class OrganizationExpense extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> claims = UserAuthentication.decodeToken(request.getHeader("Authorization"));
        String empId = claims.get("employeeId");
        String orgId = claims.get("organizationId");
        String expenseId = request.getParameter("expenseId"); 
        try {
            if(expenseId == null)
            {
                Map<Integer,Map<String, Object>> expenses = OrganizationsDAO.readOrganizationExpenses(orgId,empId);
                out.write(jsonHandler.convertMapToJson(expenses));
            }
            else
            {
                Map<String,Object> expense = OrganizationsDAO.readOrganizationExpense(orgId,empId,expenseId);
                out.write(jsonHandler.convertMapToJson(expense));
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"An error occurred while fetching expenses\"}");
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

        String type = jsonObject.getString("Type");
        String amount = jsonObject.getString("Amount");
        String date = jsonObject.getString("Date");
        String description = jsonObject.getString("Description");

        boolean success = OrganizationsDAO.createOrganizationExpense(orgId, empId, amount, type, date, description);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Expense created successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error creating expense\"}");
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
    
        String expenseId = jsonObject.getString("ExpenseId");
        double amount = jsonObject.optDouble("Amount", 0);
        String date = jsonObject.optString("Date");
        String type = jsonObject.optString("Type");
        String description = jsonObject.optString("Description");

        Map<String, Object> updates = new HashMap<>();
        if (amount > 0) updates.put("Amount", amount);
        if (!date.isEmpty()) updates.put("Date", date);
        if (!type.isEmpty()) updates.put("Type", type);
        if (!description.isEmpty()) updates.put("Description", description);

        boolean success = OrganizationsDAO.updateOrganizationExpense(orgId, empId,expenseId, updates);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Expense updated successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Error updating expense\"}");
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

        String expenseId = jsonObject.getString("ExpenseId");

        boolean success = OrganizationsDAO.deleteOrganizationExpense(orgId, expenseId);
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
            out.write("{\"message\": \"Expense deleted\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write("{\"error\": \"Expense not found\"}");
        }
    }
}
