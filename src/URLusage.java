import java.io.IOException;
import java.util.Enumeration;
import java.util.StringJoiner;

import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class URLusage extends AbstractHandler
{
    @Override
    public void handle( String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response ) throws IOException,
            ServletException
    {
        // Declare response encoding and types
//        response.setContentType("text/html; charset=utf-8");

        // Declare response status code
     //   response.setStatus(HttpServletResponse.SC_OK);

        // Write back response
        response.getWriter().write("<h1>Hello World</h1>");
      //  response.sendRedirect("http://www.google.com");
        Enumeration<String> parameterNames = request.getParameterNames();
        while(parameterNames.hasMoreElements()) {
            String name = (String) parameterNames.nextElement();
            String value = request.getParameter(name).toString();

            response.getWriter().write(String.format("%s:%s\n", name, value));
            response.getWriter().write(String.format("Method:%s<br>",request.getMethod()));
            response.getWriter().write(String.format("Method:%s<br>",request.getMethod()));
        }

        // Inform jetty that this request has now been handled
        baseRequest.setHandled(true);
    }

    public static void main( String[] args ) throws Exception
    {
        Server server = new Server(8080);

        ContextHandler context = new ContextHandler();
        context.setContextPath("/path");
        context.setHandler((new URLusage()));

        server.setHandler(context);
        server.start();
        server.join();
    }
}