import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.Enumeration;
import java.util.StringJoiner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jdk.nashorn.internal.ir.ReturnNode;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class URLusage extends AbstractHandler {
    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {
/*
        File direc = new File("./");
        System.out.println(direc.getAbsolutePath());
*/
        String x = request.getParameter("x");
        String y = request.getParameter("y");
        int xx = Integer.parseInt(x);
        int yy = Integer.parseInt(y);
        String colour = request.getParameter("colour");
        //System.out.println(x);

        String fileName = request.getPathInfo();
        String path = "src\\public\\" + fileName;

        try {
            BufferedImage img = scale(path, xx, yy);
            if (colour != null) {
                img = color(img, colour);
            }
            ImageIO.write(img, "jpg", response.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
    /*    FileInputStream bis = new FileInputStream(newPath);
        try {
            sendFile(bis,response.getOutputStream());
        }
        catch (Exception ex){
           System.out.println(ex);
        }
        */
    }

    private BufferedImage color(BufferedImage toColorImg, String color) {
        int width = toColorImg.getWidth();
        int height = toColorImg.getHeight();
        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int p = toColorImg.getRGB(yy, xx);
                int a = (p >> 24) & 0xff;
                int g = (p >> 8) & 0xff;

                p = (a<<24) |(0<<16) |g<<8 |0;
                toColorImg.setRGB(yy,xx,p);
            }
        }
        return toColorImg;
    }

    private BufferedImage scale(String path, int width, int height) throws Exception {
        BufferedImage originaImage = ImageIO.read(new File(path));
        BufferedImage resizedCopy = createResizedCopy(originaImage, width, height, true);
        return resizedCopy;
    }

    private BufferedImage createResizedCopy(Image originaImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originaImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    public static void sendFile(FileInputStream fin, OutputStream out) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);

        ContextHandler context = new ContextHandler();
        context.setContextPath("/img");
        context.setHandler((new URLusage()));

        server.setHandler(context);
        server.start();
        server.join();
    }
}