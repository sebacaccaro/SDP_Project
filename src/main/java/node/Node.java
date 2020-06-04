package node;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import gateway.store.beans.NodeBean;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import node.JoinService.ExitingResponse;
import node.JoinService.JoinResponse;
import node.JoinService.Token;
import node.JoinService.Token.TokenType;
import node.NodeDataOuterClass.NodeData;
import node.NodeServiceGrpc.NodeServiceBlockingStub;
import node.NodeServiceGrpc.NodeServiceStub;
import node.PMSensor.PM10Simulator;
import node.StatOuterClass.Stat;

public class Node {

    private final int port;
    private int id;
    private NodeBean next;
    private String ip = "localhost"; /* TODO: CHANGE WHEN REST CALL IS MADE */
    private boolean exiting = false; /* TODO: if node is exiting, don't accept requestes */
    private List<NodeBean> nodeList;
    private Server nodeServer = null;
    private SlidingWindowBuffer buffer = new SlidingWindowBuffer();
    private PM10Simulator sensor;
    StreamObserver<Token> nextNodeHandler;
    ManagedChannel nextNodeChannel = null;

    public Node(int port, int id) {
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
            nodeServer = ServerBuilder.forPort(port).addService(new NodeServiceImpl(this)).build();
            try {
                nodeServer.start();
                log("** Server started");
                nodeServer.awaitTermination();
                log("## Server terminated");
            } catch (Exception e) {
                System.err.println("An error has occured for nodeserver of " + toNodeBean());
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(r);
        serverThread.start();
    }

    public void startSensor() {
        sensor = new PM10Simulator(buffer);
        Thread sensorThread = new Thread(() -> {
            sensor.run();
        });
        sensorThread.start();
    }

    public void init() throws IOException, InterruptedException {
        startSensor();
        startServer();
        delay(2000);

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

    public synchronized void openChannelWithNode(NodeBean next) {
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
            }
        });

        log("Opened channel with N" + next.getId());
    }

    public void passNext(Token t) {
        nextNodeHandler.onNext(t);
    }

    private HashMap<Long, Token> tokenQueue = new HashMap<Long, Token>();

    public void handleToken(Token t) {
        /*
         * TODO: Commentare per fare lo sbrone
         */
        synchronized (tokenQueue) {
            tokenQueue.put(new Date().getTime(), t);
        }

        synchronized (this) {
            synchronized (tokenQueue) {
                List<Long> keys = new LinkedList<Long>(tokenQueue.keySet());
                Collections.sort(keys);
                tokenQueue.remove(keys.get(0));
            }
            log("GOT TOKEN $" + t.getType());
            delay(1500);
            switch (t.getType()) {
                case DATA:
                    t = handleAndGenerateDataToken(t);
                    passNext(t);
                    break;

                case EXIT:
                    int emitterId = t.getEmitterId();
                    if (emitterId == id) {
                        log("Getting out for good");
                        nodeServer.shutdown();
                        sensor.stopMeGently();
                    } else if (emitterId == next.getId()) {
                        passNext(t);
                        setNext(new NodeBean(t.getNext()));
                    } else {
                        passNext(t);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public Token handleAndGenerateDataToken(Token received) {
        Stat localStat = buffer.getLastStat();
        if (received.getTokenBuisy() && received.getEmitterId() == id) {
            // TODO : send token and calculate stats
            log("Sending token home" + received);
            received = Token.newBuilder().setType(TokenType.DATA).setEmitterId(id).setTokenBuisy(false).build();
        }

        if (localStat != null) {
            if (received.getTokenBuisy() == false) { /* TODO: correct type */
                return Token.newBuilder().setType(TokenType.DATA).setEmitterId(id).setTokenBuisy(true)
                        .addStat(localStat).build();
            } else {
                return received.toBuilder().addStat(localStat).setTokenBuisy(true).build();
            }
        } else {
            return received;
        }
    }

    public void exitRing() {
        log("Emitting leave token");
        Token exitToken = Token.newBuilder().setType(TokenType.EXIT).setEmitterId(id).setNext(next.toNodeData())
                .build();
        exiting = true;
        passNext(exitToken);
    }

    public void log(String toLog) {
        System.out.println("N" + id + " >> " + toLog);
    }

    public static void delay(int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}