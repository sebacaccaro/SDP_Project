package server;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.net.httpserver.HttpServer;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class MyResourceTest {

    private static final String HOST = "localhost";
    private static final int PORT = 1337;

    public static void main(String[] args) throws IOException {
        URI baseUri = UriBuilder.fromUri("http://" + HOST + "/").port(PORT).build();
        ResourceConfig config = new ResourceConfig(HelloWorldResource.class);
        HttpServer server = JdkHttpServerFactory.createHttpServer(baseUri, config);

        System.out.println("Server running!");
        System.out.println("Server started on: http://" + HOST + ":" + PORT);

        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        server.stop(0);
        System.out.println("Server stopped");

    }
}