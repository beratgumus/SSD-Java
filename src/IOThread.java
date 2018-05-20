import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Callable;

public class IOThread implements Callable {
    String remoteUrl = "http://bihap.com/img";
    boolean connectRemote = false;
    boolean ActivateCache = true;
    HashMap<String, BufferedImage> imgCache = new HashMap<>();
    boolean lockImgCache = false;
    Job job;
    BufferedImage img;

    public IOThread(Job job) {
        this.job = job;
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

    @Override
    public Object call() throws Exception {
        String name = Thread.currentThread().getName();
        System.out.println(name+" IO started");
        synchronized (job) {
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
            //System.out.println(name+" IO Notifier work done");
           // job.notify();

        }
        System.out.println(name+" IO ended");
        return job;
    }
}
