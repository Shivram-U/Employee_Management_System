package Authentication;

import DBUtil.*;
import JSON.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JWTAuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        PrintWriter out = response.getWriter();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getServletPath();
        String method = httpRequest.getMethod();

        // Allow POST requests to /Organization without token
        if( ("/signup".equals(path) && !"POST".equalsIgnoreCase(method)) ||
            ("/signin".equals(path) && !"POST".equalsIgnoreCase(method)) ||
            ("/Organization".equals(path) && "POST".equalsIgnoreCase(method)))
            return;

        if (("/signup".equals(path) && "POST".equalsIgnoreCase(method)) || 
            ("/signin".equals(path) && "POST".equalsIgnoreCase(method))) {
            chain.doFilter(request, response);
            return;
        }

        // Check for Authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader;
            Map<String,String> claims = UserAuthentication.decodeToken(token);
            String empId = claims.get("employeeId");
            String orgId = claims.get("organizationId");
            String userRole = EmployeeDAO.getRoleName(empId,orgId);

            if(UserAuthentication.validateToken(token) && OrganizationsDAO.doesOrganizationExist(orgId) && EmployeeDAO.doesEmployeeExist(orgId,empId))
            {
                boolean handled = false;
                if("/Organization".equals(path) || "/OrgPhn".equals(path))
                {
                    if (userRole.equals("CEO")) 
                        handled = true;
                    else
                    {
                        if("GET".equalsIgnoreCase(method))
                            handled = true;
                    }
                }
                else if("/OrgExp".equals(path) || "/OrgEml".equals(path))
                {
                    if (userRole.equals("CEO")) 
                        handled = true;
                }
                else if("/Profile".equals(path))
                {
                    handled = true;
                }
                else if("/Employee".equals(path))
                {
                    if("POST".equalsIgnoreCase(method))
                    {
                        if(userRole.equals("CEO") || (userRole.equals("Manager")))
                            handled = true;
                    }
                    else
                    {
                        String empRole,tempId;
                        tempId = request.getParameter("EmployeeId");
                        empRole = EmployeeDAO.getRoleName(tempId,orgId);

                        if (userRole.equals("CEO") || (userRole.equals("Manager") && !empRole.equals("Manager") && !empRole.equals("CEO"))) 
                        {
                            if(!tempId.equals(empId))
                                handled = true;
                        }
                    }
                }
                else if("/Department".equals(path) || "/Role".equals(path) || "/Asset".equals(path))
                {   
                    if (userRole.equals("CEO")) 
                    {
                        handled = true;
                    }
                }
                else if("/DepartmentMgmt".equals(path) || "/RoleMgmt".equals(path) || "/AssetMgmt".equals(path))
                {   
                    if (userRole.equals("CEO") || userRole.equals("Manager")) 
                    {
                        String empRole,tempId;
                        tempId = request.getParameter("EmployeeId");
                        empRole = EmployeeDAO.getRoleName(tempId,orgId);
                        if (userRole.equals("CEO") || (userRole.equals("Manager") && !empRole.equals("Manager") && !empRole.equals("CEO"))) 
                        {
                            if(!tempId.equals(empId))
                                handled = true;
                        }
                    }
                }
                else if("/Role".equals(path))
                {   
                    if (userRole.equals("CEO")) 
                    {
                        handled = true;
                    }
                }
                else if("/Asset".equals(path))
                {
                    if (userRole.equals("CEO")) 
                    {
                        handled = true;
                    }
                }
                else if("/Payroll".equals(path))
                {
                    if (userRole.equals("CEO")) 
                    {
                        handled = true;
                    }
                }
                else if("/EmployeePhn".equals(path) || "/EmployeeEml".equals(path))
                {
                    handled = true;
                }
                else if("/EmpAttd".equals(path))
                {
                    String empRole,tempId;
                    tempId = request.getParameter("EmployeeId");
                    String managerId = EmployeeDAO.getManagerIDOfEmployeeDepartment(orgId,tempId);
                    empRole = EmployeeDAO.getRoleName(tempId,orgId);
                    if (userRole.equals("CEO") || (userRole.equals("Manager") && !empRole.equals("Manager") && !empRole.equals("CEO") && managerId!=null && managerId.equals(empId)))
                    {
                        if(!tempId.equals(empId))
                            handled = true;
                    }
                    else if(tempId.equals(empId) && method.equalsIgnoreCase("GET"))
                    {
                        handled = true;
                    }
                }
                else if("/Holiday".equals(path))
                {
                    if(userRole.equals("CEO"))
                        handled = true;
                }
                else if("/EmpLeav".equals(path))
                {
                    handled = true;
                }
                else if("/EmpLeavMgmt".equals(path))
                {
                    String empRole,tempId;
                    tempId = request.getParameter("EmployeeId");
                    String managerId = EmployeeDAO.getManagerIDOfEmployeeDepartment(orgId,tempId);
                    empRole = EmployeeDAO.getRoleName(tempId,orgId);
                    if (userRole.equals("CEO") || (userRole.equals("Manager") && !empRole.equals("Manager") && !empRole.equals("CEO") && managerId!=null && managerId.equals(empId)))
                    {
                        if(!tempId.equals(empId))
                            handled = true;
                    }
                }
                if(handled)
                {
                    chain.doFilter(request, response);
                }
                else
                {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write("{\"error\": \"Access denied\"}");
                    return;
                }
            } 
            else
            {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write("Invalid token");
            }
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.write("Authorization header is missing or invalid");
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
