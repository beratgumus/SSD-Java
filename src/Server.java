import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.preventers.AppContextLeakPreventer;


public class Server extends AbstractHandler {
    ExecutorService ioService;
    ExecutorService scaleService;

    public Server() {
        ioService = Executors.newFixedThreadPool(10);
        scaleService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {

        Job job = new Job(target, baseRequest, request, response);



        ImageIO.write(job.getImage(), "jpg", response.getOutputStream());

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

                p = (a << 24) | (0 << 16) | g << 8 | 0;
                toColorImg.setRGB(yy, xx, p);
            }
        }
        return toColorImg;
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
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(8080);
        server.addBean(new AppContextLeakPreventer());

        ContextHandler context = new ContextHandler();
        context.setContextPath("/img");
        context.setHandler((new Server()));

        server.setHandler(context);
        server.start();
        server.join();
    }
}