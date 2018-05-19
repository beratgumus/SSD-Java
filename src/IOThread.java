import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;

public class IOThread implements Runnable {
    String remoteUrl = "http://bihap.com/img";
    boolean connectRemote = true;
    boolean ActivateCache = true;
    HashMap<String, BufferedImage> imgCache = new HashMap<>();
    boolean lockImgCache = false;
    Job job;
    BufferedImage img;

    public IOThread(Job job) {
        this.job = job;
    }

    @Override
    public void run() {
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
