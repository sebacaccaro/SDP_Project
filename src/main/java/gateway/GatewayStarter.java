package gateway;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import gateway.resources.*;

public class GatewayStarter {

    private static final String HOST = "localhost";
    private static final int PORT = 1337;

    public static void main(String[] args) throws IOException {
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (IndexOutOfBoundsException e) {
            port = PORT;
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number/format");
            System.exit(1);
        }

        URI baseUri = UriBuilder.fromUri("http://" + HOST + "/").port(port).build();

        Set<Class<?>> resources = new HashSet<Class<?>>();
        resources.add(NodeResource.class);
        resources.add(AnalystResource.class);

        ResourceConfig config = new ResourceConfig(resources);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        System.out.println("######################################");
        System.out.println("#######   SDP Project Gateway  #######");
        System.out.println("#### Sebastiano Caccaro AA.18/19 #####");
        System.out.println("######################################");
        System.out.println("\n > Gateway started on: http://" + HOST + ":" + port + "\n");

        System.out.println("------   Hit return to stop ----------\n");
        System.in.read();
        System.out.println("> Stopping server");
        server.shutdown();
        System.out.println("> Server stopped");
        System.exit(0);

    }
}