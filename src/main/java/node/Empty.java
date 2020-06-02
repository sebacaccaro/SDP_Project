package node;

import java.io.IOException;

import node.JoinService.Token;

public class Empty {
    public static void main(String[] args) throws IOException, InterruptedException {
        Node n1 = new Node(9999, 1);
        Node n2 = new Node(8888, 2);

        n1.init();
        n2.init();
        Thread.sleep(3000);
        System.out.println("TIMEOUTOVER");
        n2.openChannelWithNode(n1.toNodeBean());
        n1.openChannelWithNode(n2.toNodeBean());
        Token t1 = Token.newBuilder().setRandomShit("Token1").build();
        Thread.sleep(3000);
        n2.passNext(t1);
    }
}