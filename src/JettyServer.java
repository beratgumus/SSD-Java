import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.*;

import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.preventers.AppContextLeakPreventer;


public class JettyServer extends HttpServlet {
    String remoteUrl = "http://bihap.com/img";

    boolean connectRemote = true;
    boolean ActivateCache = false;
    HashMap<String, BufferedImage> imgCache = new HashMap<>();

    ExecutorService ioService;
    ExecutorService scaleService;
    final BlockingQueue<Job> ioQueue;
    final BlockingQueue<Job> scaleQueue;

    public JettyServer() {
        ioService = Executors.newFixedThreadPool(30);
        scaleService = Executors.newFixedThreadPool(10);
        ioQueue = new LinkedBlockingQueue<>();
        scaleQueue = new LinkedBlockingQueue<>();
    }

    protected void doGet(
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        AsyncContext async = request.startAsync();

        Job job = new Job(request, response, async);

        ioQueue.add(job);
        ioService.execute(new IOThread());


    }

    public static void main(String[] args) throws Exception {

        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.setConnectors(new Connector[] {connector});

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(
                JettyServer.class, "/img/*");

        server.setHandler(servletHandler);

        server.start();

    }

    public class IOThread implements Runnable {
        BufferedImage img;
        boolean lockImgCache = false;

        @Override
        public void run() {

            Job job = ioQueue.poll();

            if (ActivateCache) {
                img = imgCache.get(job.getFilename());

                if (img == null) {
                    try {
                        img = loadImg(job.getFilename());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }

                    if (!lockImgCache) {
                        lockImgCache = true;
                        imgCache.put(job.getFilename(), img);
                        lockImgCache = false;
                    }
                }
            } else {
                try {
                    img = loadImg(job.getFilename());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            job.setImage(img);
            scaleQueue.add(job);
            scaleService.execute(new ScaleThread());

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
    }

    public class ScaleThread implements Runnable {

        @Override
        public void run() {

            Job job = scaleQueue.poll();
            BufferedImage img = null;
            try {
                img = scale(job.getImage(), job.getWidth(), job.getHeight());


                if (img != null) {
                    job.setImage(img);
                    job.onWritePossible();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //ImageIO.write(img, "jpg", response.getOutputStream());




/*
                        if (job != null && job.getColor() != null && job.getColor().equals("gray")) {
                            // color parametresi gray ise renk değiştireceğiz
                            img = grayScale(img);
                        }
                        */

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

        private BufferedImage scale(BufferedImage originalImg, int width, int height) throws Exception {

            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaledImage.createGraphics();

            g.setComposite(AlphaComposite.Src);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            g.drawImage(originalImg, 0, 0, width, height, null);
            g.dispose();

            return scaledImage;
        }
    }
}