package node;

import java.io.IOException;

import node.JoinService.Token;

public class Empty {
    public static void main(String[] args) throws IOException, InterruptedException {
        Node n1 = new Node(1111, 1);
        Node n2 = new Node(2222, 2);
        Node n3 = new Node(3333, 3);

        n1.init();
        Thread.sleep(1000);
        n2.init();
        Thread.sleep(1000);
        n3.init();

        Thread.sleep(1000);
        Token t1 = Token.newBuilder().setRandomShit("Token1").build();
        n1.passNext(t1);

    }
}