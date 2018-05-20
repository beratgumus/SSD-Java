import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class IOThread implements Runnable {
    String remoteUrl = "http://bihap.com/img";
    boolean connectRemote = true;
    boolean ActivateCache = true;
    HashMap<String, BufferedImage> imgCache = new HashMap<>();
    boolean lockImgCache = false;
    final BlockingQueue<Job> ioQueue;
    final BlockingQueue<Job> scaleQueue;
    BufferedImage img;

    public IOThread(BlockingQueue<Job> ioQueue, BlockingQueue<Job> scaleQueue) {
        this.ioQueue = ioQueue;
        this.scaleQueue = scaleQueue;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (ioQueue) {
                Job job = null;
                try {
                    job = ioQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
                    try {
                        job.setImage(img);
                        scaleQueue.put(job);
                        job.wait();
                        System.out.println("ioQueue: "+ioQueue.size());
                        System.out.println("scaleQueue: "+scaleQueue.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
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
