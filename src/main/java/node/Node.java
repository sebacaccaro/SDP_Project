package node;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import gateway.store.beans.NodeBean;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import node.JoinService.JoinResponse;
import node.NodeDataOuterClass.NodeData;
import node.NodeServiceGrpc.NodeServiceBlockingStub;

public class Node {

    private final int port;
    private int id;
    private NodeBean next;
    private String ip = "localhost"; /* TODO: CHANGE WHEN REST CALL IS MADE */
    private List<NodeBean> nodeList;

    private static int count = -1;

    public Node(int port) {
        count++;
        this.port = port;
        this.id = new Random().nextInt(256);
        // TODO: remove fixed next node
        if (count == 0) {
            NodeBean self = new NodeBean();
            self.setId(6969);
            self.setIp("testID");
            self.setPort(4242);
            next = self;
        } else {
            NodeBean self = new NodeBean();
            self.setId(1111);
            self.setIp("testLOL");
            self.setPort(7777);
            next = self;
        }
        //

    }

    public NodeBean toNodeBean() {
        NodeBean self = new NodeBean();
        self.setId(id);
        self.setIp(ip);
        self.setPort(port);
        return self;
    }

    public NodeData toNodeData() {
        return this.toNodeBean().toNodeData();
    }

    public NodeBean getNext() {
        return next;
    }

    public void setNext(NodeBean newNext) {
        next = newNext;
    }

    public void init() throws IOException, InterruptedException {
        Runnable r = () -> {
            Server server = ServerBuilder.forPort(port).addService(new NodeServiceImpl(this)).build();
            try {
                server.start();
                System.out.println("** Server started for " + toNodeBean());
                server.awaitTermination();
            } catch (Exception e) {
                System.err.println("An error has occured for nodeserver of " + toNodeBean());
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(r);
        serverThread.start();

    }

    public void joinAfter(NodeBean nodeToAsk) {
        System.out.println("MAKING JOINAFTER REQUEST");
        System.out.println("Old next: " + next);
        ManagedChannel channel = ManagedChannelBuilder.forTarget(nodeToAsk.fullAddresse()).usePlaintext(true).build();
        NodeServiceBlockingStub stub = NodeServiceGrpc.newBlockingStub(channel);
        JoinResponse response = stub.joinAfter(nodeToAsk.toNodeData());
        if (response.getJoinApproved()) {
            next = new NodeBean(response.getNextNode());
            System.out.println("JOINAFTER REQUEST DISPATCHED");
            System.out.println("New next: " + next);
            // TODO: open connection
        } else {
            // TODO: decide what to do
        }
    }

}