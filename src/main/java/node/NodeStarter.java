package node;

import java.io.IOException;

public class NodeStarter {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = 0;
        int id = 0;
        try {
            port = Integer.parseInt(args[0]);
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port/id number/format");
            System.exit(1);
        }
        try {
            int serverPort = Integer.parseInt(args[2]);
            String serverUrl = args[3];
            new Node(port, id, "localhost", serverUrl, serverPort).init();
        } catch (IndexOutOfBoundsException e) {
            new Node(port, id).init();
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number/format");
            System.exit(1);
        }
    }
}