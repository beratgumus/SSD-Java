import org.eclipse.jetty.server.Request;

import javax.imageio.ImageIO;
import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Job implements WriteListener {
    HttpServletRequest request;
    HttpServletResponse response;
    BufferedImage img;
    ServletOutputStream out;
    AsyncContext async;

    public Job(HttpServletRequest request, HttpServletResponse response, AsyncContext async) {

        this.request = request;
        this.response = response;
        this.async = async;
        try {
            out = response.getOutputStream();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onWritePossible() throws IOException {
//        while (out.isReady()) {
//            if (!content.hasRemaining()) {
//                response.setStatus(200);
//                async.complete();
//                return;
//            }
            response.setStatus(200);
            response.setContentType("img/jpg");
            ImageIO.write(img, "jpg", out);

            async.complete();

            //out.write(content.get());
//        }
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(t.getMessage());
        async.complete();
    }

    public int getWidth() {
        return Integer.parseInt(request.getParameter("width"));
    }

    public int getHeight() {
        return Integer.parseInt(request.getParameter("height"));
    }

    public String getFilename() {
        return request.getPathInfo();
    }

    public String getColor() {
        return request.getParameter("color").toLowerCase();
    }

    public void setImage(BufferedImage img) {
        this.img = img;
    }

    public BufferedImage getImage() {
        return img;
    }

}
