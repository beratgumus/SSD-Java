import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.javafx.font.directwrite.RECT;
import jdk.nashorn.internal.ir.ReturnNode;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.w3c.dom.css.Rect;

public class URLusage extends AbstractHandler {

    private String remoteUrl = "http://bihap.com/img";
    private boolean connectRemote = true;


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
        //Map params = request.getParameterMap();


        int x = Integer.parseInt(request.getParameter("width"));
        int y = Integer.parseInt(request.getParameter("height"));

        String color = null;
        if (request.getParameterMap().containsKey("color")){
            color = request.getParameter("color").toLowerCase();
        }
        //System.out.println(x);

        String fileName = request.getPathInfo();

        try {
            // her türlü yeniden boyutlandırma yapacağız
            BufferedImage img = scale(fileName, x, y);
            if (color != null && color.equals("gray")) {
                // color parametresi gray ise renk değiştireceğiz
                img = grayScale(img);
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


    // TODO: (maybe) accept any color: http://www.java2s.com/Tutorials/Java/Graphics_How_to/Draw/Colorize_a_picture.htm
    // şimdilik bunu kullanmıyoruz. bunun yavaş bir yöntem olduğunu söylüyorlar.
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

    // resmi griye çevirip gri resmi döndürür
    private BufferedImage grayScale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    //Resmi uzak sunucudan veya yerelden yükler
    private BufferedImage loadImg(String fileName) throws Exception {
        if (connectRemote) {
            URL url = new URL(remoteUrl + fileName);
            return ImageIO.read(url);
        } else {
            String path = "src\\public\\" + fileName;
            return ImageIO.read(new File(path));
        }

    }

    private BufferedImage scale(String path, int width, int height) throws Exception {
        BufferedImage originaImage = loadImg(path);
        //BufferedImage resizedCopy = createResizedCopy(originaImage, width, height, true);

        //int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();

        g.setComposite(AlphaComposite.Src);

        g.drawImage(originaImage, 0, 0, width, height, null);
        g.dispose();
        return scaledImage;
    }

    // DEPRECATED:
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