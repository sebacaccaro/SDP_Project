package node;

import java.io.IOException;

public class Empty {
    public static void main(String[] args) throws IOException, InterruptedException {
        Node n1 = new Node(9999);
        Node n2 = new Node(8888);

        n1.init();
        Thread.sleep(3000);
        System.out.println("TIMEOUTOVER");
        n2.joinAfter(n1.toNodeBean());
    }
}