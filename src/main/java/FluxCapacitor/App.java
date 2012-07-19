package FluxCapacitor;

import FluxCapacitor.util.FluxConfiguration;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;


public class App
{
    private static void handleConfig(String[] args) throws IOException{
        FluxConfiguration fluxConfiguration = FluxConfiguration.getInstance();
        fluxConfiguration.set("jobtracker","master:54311");
        fluxConfiguration.set("hdfs","hdfs://master:54310/");
    }

    public static void main(String[] args) throws Exception
    {
        handleConfig(args);
        Server server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8080);
        server.addConnector(connector);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase("./src/main/static");

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        ServletContainer container = new ServletContainer();
        ServletHolder h = new ServletHolder(container);

        h.setInitParameter("com.sun.jersey.config.property.packages","FluxCapacitor.services");
        h.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        context.addServlet(h,"/*");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, context, new DefaultHandler() });
        server.setHandler(handlers);

        server.start();
        server.join();
    }
}
