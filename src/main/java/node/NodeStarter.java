package node;

import java.io.IOException;

public class NodeStarter {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[0]);
        int id = Integer.parseInt(args[1]);
        try {
            String ip = args[3];
            int serverPort = Integer.parseInt(args[3]);
            String serverUrl = args[4];
            new Node(port, id, ip, serverUrl, serverPort).init();
        } catch (IndexOutOfBoundsException e) {
            new Node(port, id).init();
        }
    }
}