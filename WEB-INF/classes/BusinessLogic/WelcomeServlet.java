package BusinessLogic;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ReadOnlyBufferException;
import javax.naming.ldap.PagedResultsResponseControl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
Command : javac -classpath :/Applications/Tomcat/lib/tomcat-servlet-api-9.0.4.jar -d . WelcomeServlet.java 
*/

public class WelcomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Set the content type of the response
        PrintWriter out = response.getWriter();
        try
        {
            response.setContentType("text/html");
            out.println("welcome");
            request.getRequestDispatcher("/welcome.html").include(request, response);
        }
        catch(Exception e)
        {
            out.println(e.getMessage());
        }
    }
    @Override 
    public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }
}
