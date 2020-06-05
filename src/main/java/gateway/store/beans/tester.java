package gateway.store.beans;

import gateway.ConcurrentStructures.DuplicateKeyException;
import gateway.store.Store;

public class tester {
    public static void main(String[] args) throws InterruptedException {
        Store s = new Store();
        NodeBean n1 = new NodeBean();
        n1.setId(1);
        n1.setIp("ip1");
        n1.setPort(1);
        NodeBean n2 = new NodeBean();
        n2.setId(2);
        n2.setIp("ip2");
        n2.setPort(2);
        NodeBean n3 = new NodeBean();
        n3.setId(3);
        n3.setIp("ip3");
        n3.setPort(3);
        NodeBean n4 = new NodeBean();
        n4.setId(4);
        n4.setIp("ip4");
        n4.setPort(4);
        NodeBean n5 = new NodeBean();
        n5.setId(5);
        n5.setIp("ip5");
        n5.setPort(5);
        NodeBean n6 = new NodeBean();
        n6.setId(5);
        n6.setIp("ip6");
        n6.setPort(6);
        try {
            s.addNode(n1);
            s.addNode(n2);
            s.addNode(n3);
            s.addNode(n4);
            s.addNode(n5);
            s.addNode(n6);
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
        }
        for (NodeBean n : s.getNodes().getNodes()) {
            System.out.println("ID: " + n.getId() + " IP: " + n.getIp() + "Port: " + n.getPort());
        }
        ;

    }
}