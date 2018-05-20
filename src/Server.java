import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.preventers.AppContextLeakPreventer;

public class Server extends AbstractHandler {
    private ExecutorService ioService;
    private ExecutorService scaleService;

    public Server() {
        ioService = Executors.newFixedThreadPool(10);
        scaleService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {

        Job job = new Job(target, baseRequest, request, response);
        Future submit = scaleService.submit(new ScaleThread(job, ioService));

        try {
            Job resizeJob = (Job)submit.get();
            ImageIO.write(resizeJob.getImage(), "jpg", resizeJob.getResponse().getOutputStream());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

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