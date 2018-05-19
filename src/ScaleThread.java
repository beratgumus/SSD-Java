import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScaleThread implements Runnable {
    Job job;

    public ScaleThread(Job job) {
        this.job = job;
    }

    @Override
    public void run() {
        BufferedImage img=null;
        try {
            img = scale(job.getImage(), job.getWidth(), job.getHeight());


        if (job.getColor() != null && job.getColor().equals("gray")) {
            // color parametresi gray ise renk değiştireceğiz
            img = grayScale(img);
        }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //ImageIO.write(img, "jpg", response.getOutputStream());
        if (img!= null)
            job.setImage(img);
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
