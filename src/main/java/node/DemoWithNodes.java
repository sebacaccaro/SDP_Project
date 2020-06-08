package node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import gateway.store.beans.StatUnitBean;
import node.JoinService.Token;
import node.JoinService.Token.Builder;
import node.JoinService.Token.TokenType;

public class DemoWithNodes {
    public static void main(String[] args) throws IOException, InterruptedException {
        // Just a demo used for testing

        int num = 40;
        List<Node> ln = new LinkedList<Node>();
        for (int i = 2001; i < 2001 + 40; i++)
            ln.add(new Node(i, i - 2000));

        for (Node n : ln) {
            n.init();
        }

        Thread.sleep(10000);
        ln.get(5).exitRing();

    }
}