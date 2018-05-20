import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ScaleThread implements Callable {
    private Job job;
    private ExecutorService ioService;

    public ScaleThread(Job job, ExecutorService ioService) {
        this.job = job;
        this.ioService = ioService;
    }

    // resmi griye çevirip gri resmi döndürü
    private BufferedImage grayScale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = result.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

    private BufferedImage scale(BufferedImage originalImg, int width, int height){

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g.drawImage(originalImg, 0, 0, width, height, null);
        g.dispose();

        return scaledImage;
    }

    @Override
    public Object call() throws Exception {

        Future submit = ioService.submit(new IOThread(job));
        Job ioJob = null;

        try {
            ioJob = (Job) submit.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        BufferedImage img = null;
        try {
            String name = Thread.currentThread().getName();
            System.out.println(name + " Scale started at time: " + System.currentTimeMillis());
            img = scale(ioJob.getImage(), ioJob.getWidth(), ioJob.getHeight());

            if (ioJob.getColor() != null && ioJob.getColor().equals("gray")) {
                // color parametresi gray ise renk değiştireceğiz
                img = grayScale(img);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //ImageIO.write(img, "jpg", response.getOutputStream());
        if (img != null)
            job.setImage(img);

        return job;
    }
}
