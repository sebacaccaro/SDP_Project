package node;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import gateway.store.beans.NodeBean;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import node.JoinService.ExitingResponse;
import node.JoinService.JoinResponse;
import node.JoinService.Token;
import node.NodeDataOuterClass.NodeData;
import node.NodeServiceGrpc.NodeServiceBlockingStub;
import node.NodeServiceGrpc.NodeServiceStub;

public class Node {

    private final int port;
    private int id;
    private NodeBean next;
    private String ip = "localhost"; /* TODO: CHANGE WHEN REST CALL IS MADE */
    private List<NodeBean> nodeList;
    private Token token;
    StreamObserver<Token> nextNodeHandler;
    ManagedChannel nextNodeChannel = null;

    private static int count = -1;

    public Node(int port, int id) {
        count++;
        this.port = port;
        this.id = id;
        this.next = this.toNodeBean();
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
        Thread channelThread = new Thread(() -> {
            openChannelWithNode(next);
        });
        channelThread.start();
    }

    public void startServer() {
        Runnable r = () -> {
            Server server = ServerBuilder.forPort(port).addService(new NodeServiceImpl(this)).build();
            try {
                server.start();
                log("** Server started");
                server.awaitTermination();
                log("## Server terminated");
            } catch (Exception e) {
                System.err.println("An error has occured for nodeserver of " + toNodeBean());
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(r);
        serverThread.start();
    }

    public void init() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(2000);

        MockServer ms = new MockServer();
        List<NodeBean> nodes = ms.register(this.toNodeBean());
        joinAfter(nodes.get(0));
    }

    public void joinAfter(NodeBean nodeToAsk) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(nodeToAsk.fullAddresse()).usePlaintext(true).build();
        NodeServiceBlockingStub stub = NodeServiceGrpc.newBlockingStub(channel);
        JoinResponse response = stub.joinAfter(this.toNodeData());
        if (response.getJoinApproved()) {
            setNext(new NodeBean(response.getNextNode()));
        } else {
            // TODO: decide what to do
        }
        channel.shutdown();
    }

    public void openChannelWithNode(NodeBean next) {
        if (nextNodeChannel != null) {
            nextNodeHandler.onCompleted();
            nextNodeChannel.shutdown();
        }
        nextNodeChannel = ManagedChannelBuilder.forTarget(next.fullAddresse()).usePlaintext(true).build();
        NodeServiceStub stub = NodeServiceGrpc.newStub(nextNodeChannel);
        nextNodeHandler = stub.passNext(new StreamObserver<ExitingResponse>() {

            @Override
            public void onNext(ExitingResponse value) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(Throwable t) {
                // TODO Auto-generated method stub
                log("Errore !");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // TODO Auto-generated method stub
                log("Completato!");

            }
        });

        log("Opened channel with N" + next.getId());
    }

    public void passNext(Token t) {
        nextNodeHandler.onNext(t);
    }

    public void handleToken(Token t) {
        // TODO: implement
        // 1. If I can write, I write into the token
        // 2. Pass the token next
        log("GOT TOKEN $" + t.toString());
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        passNext(t);
    }

    public void log(String toLog) {
        System.out.println("N" + id + " >> " + toLog);
    }

}