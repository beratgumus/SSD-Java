import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
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
    String remoteUrl = "http://bihap.com/img";
    boolean connectRemote = true;
    boolean ActivateCache = true;
    HashMap<String, BufferedImage> imgCache = new HashMap<>();


    ExecutorService ioService;
    ExecutorService scaleService;
    final BlockingQueue<Job> ioQueue;
    final BlockingQueue<Job> scaleQueue;

    public Server() {
        ioService = Executors.newFixedThreadPool(30);
        scaleService = Executors.newFixedThreadPool(10);
        ioQueue = new LinkedBlockingQueue<>();
        scaleQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {

        Job job = new Job(request);
        ioQueue.add(job);
        ioService.execute(new IOThread());

        synchronized (job) {
            try {
                job.wait();
                ImageIO.write(job.getImage(), "jpg", response.getOutputStream());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
/*
        while (resultQueue.isEmpty()) {
            try {
                synchronized (resultQueue) {
                    resultQueue.wait();
                    Job result = resultQueue.take();
                    ImageIO.write(result.getImage(), "jpg", response.getOutputStream());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
/*
                        if (job != null && job.getColor() != null && job.getColor().equals("gray")) {
                            // color parametresi gray ise renk değiştireceğiz
                            img = grayScale(img);
                        }
                        */
            } catch (Exception e) {
                e.printStackTrace();
            }
            //ImageIO.write(img, "jpg", response.getOutputStream());
            if (img != null)
                job.setImage(img);

            synchronized (job) {
                job.notify();
            }
                    /*

                    try {
                        ImageIO.write(job.getImage(), "jpg", job.getResponse().getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/


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