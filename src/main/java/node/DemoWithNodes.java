package node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import gateway.store.beans.StatUnitBean;
import node.JoinService.Token;
import node.JoinService.Token.Builder;
import node.JoinService.Token.TokenType;

public class DemoWithNodes {
    public static void main(String[] args) throws IOException, InterruptedException {
        /*
         * Node n1 = new Node(1111, 1); Node n2 = new Node(2222, 2); Node n3 = new
         * Node(3333, 3); Node n4 = new Node(4444, 4);
         * 
         * n1.init(); n2.init(); n3.init(); n4.init();
         * 
         * Thread.sleep(15000); n2.exitRing(); n1.exitRing();
         */

        List<Node> ln = new LinkedList<Node>();
        for (int i = 2001; i < 2001 + 40; i++)
            ln.add(new Node(i, i - 2000));

        for (Node n : ln) {
            n.init();
        }

    }
}