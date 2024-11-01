package BusinessLogic;

import DBUtil.*;
import Authentication.*;
import JSON.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Auth extends HttpServlet {
    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject jsonObject = jsonHandler.parseJsonBody(req);

        String orgId = jsonObject.getString("organizationId");
        String empId = jsonObject.getString("employeeId");
        String password = jsonObject.getString("password");

        // Dummy authentication logic (replace with real authentication)
        if (EmployeeDAO.verifyUser(orgId,empId,password)) {

            // Generate token with EmployeeID and OrganizationID
            String token = UserAuthentication.generateToken(empId, orgId);
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"token\": \"" + token + "\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("Invalid credentials");
        }
    }
}
