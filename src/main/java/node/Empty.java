package node;

import java.io.IOException;

import gateway.store.beans.StatUnitBean;
import node.JoinService.Token;
import node.JoinService.Token.Builder;

public class Empty {
    public static void main(String[] args) throws IOException, InterruptedException {
        Node n1 = new Node(1111, 1);
        Node n2 = new Node(2222, 2);
        Node n3 = new Node(3333, 3);
        Node n4 = new Node(4444, 4);

        n1.init();

        /*
         * Thread.sleep(1000); n2.init(); Thread.sleep(1000); n3.init();
         * Thread.sleep(1000); n4.init(); Thread.sleep(5000);
         */
        Thread.sleep(2000);
        Token t1 = Token.newBuilder().setEmitterId(800).setTokenBuisy(false).build();
        n1.passNext(t1);

    }
}