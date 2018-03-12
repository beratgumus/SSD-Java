import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.logging.Logger;

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
/*
        File direc = new File("./");
        System.out.println(direc.getAbsolutePath());
*/
        FileInputStream bis = new FileInputStream("src\\public\\yose.jpg");
        try {
            sendFile(bis,response.getOutputStream());
        }
        catch (Exception ex){
           System.out.println(ex);
        }
    }

    public static void sendFile(FileInputStream fin, OutputStream out) throws Exception{
        byte[] buffer =new byte[1024];
        int bytesRead;
        while ((bytesRead = fin.read(buffer)) != -1){
            out.write(buffer,0,bytesRead);
        }
        fin.close();
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