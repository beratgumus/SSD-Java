import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;

public class Job {
    String target;
    Request baseRequest;
    HttpServletRequest request;
    HttpServletResponse response;
    BufferedImage img;

    public Job(String target,
               Request baseRequest,
               HttpServletRequest request,
               HttpServletResponse response) {
        this.target = target;
        this.baseRequest = baseRequest;
        this.request = request;
        this.response = response;
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
        if (request.getParameter("color") == null)
            return null;
        return request.getParameter("color").toLowerCase();
    }

    public void setImage(BufferedImage img) {
        this.img = img;
    }

    public BufferedImage getImage() {
        return img;
    }

    public HttpServletResponse getResponse() {
        return response;
    }
}
