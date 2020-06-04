package node;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import gateway.store.beans.NodeBean;
import gateway.store.beans.NodeBeanList;
import gateway.store.beans.StatUnitBean;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import node.JoinService.ExitingResponse;
import node.JoinService.JoinResponse;
import node.JoinService.Token;
import node.JoinService.Token.Builder;
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
    private boolean hasSkipBadge = false;
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

        WebTarget gatewayPath = ClientBuilder.newClient().target("http://localhost:1337/node/join");
        Invocation.Builder invocationBuilder = gatewayPath.request(MediaType.APPLICATION_JSON);
        List<NodeBean> nodes = invocationBuilder.post(Entity.json(this.toNodeBean()), NodeBeanList.class).getNodes();

        // MockServer ms = new MockServer();
        // List<NodeBean> nodes = ms.register(this.toNodeBean());
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
            // log("GOT TOKEN $" + t.getType());
            delay(10);
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
                        WebTarget gatewayPath = ClientBuilder.newClient()
                                .target("http://localhost:1337/node/leave/" + id);
                        Invocation.Builder invocationBuilder = gatewayPath.request(MediaType.APPLICATION_JSON);
                        Response r = invocationBuilder.delete();
                        log("" + r.getStatus());
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
        List<Integer> written = new LinkedList<Integer>(received.getWritesList());
        List<Integer> skipped = new LinkedList<Integer>(received.getSkipsList());

        if (exiting && !skipped.contains(id)) {
            return received;
        }

        if (written.contains(id)) {
            if (skipped.size() == 0) {
                // Token is full: I send it and emit a new one
                sendStatToGateway(mean(received.getStatList()));
                return Token.newBuilder().setType(TokenType.DATA).build();
            } else {
                // I don't have to do anything, I just pass on the token
                return received;
            }
        } else {
            if (buffer.isValueProduced()) {
                // Metto valore dentro e rimuovo il mio eventuale skip
                skipped.remove(new Integer(id));
                return received.toBuilder().addStat(buffer.getLastStat()).clearSkips().addAllSkips(skipped)
                        .addWrites(id).build();
            } else {
                int skipIndex = skipped.indexOf(id);
                Builder b = received.toBuilder();
                if (skipIndex == -1) {
                    b.addSkips(id);
                }
                return b.build();
            }
        }
    }

    public StatUnitBean mean(List<Stat> stats) {
        double value = 0;
        long timestamp = 0;
        for (Stat s : stats) {
            value += s.getValue();
            timestamp += s.getTimestamp();
        }
        StatUnitBean m = new StatUnitBean();
        m.setValue(value / stats.size());
        m.setTimestamp(timestamp = (long) (timestamp * 1.0 / stats.size()));
        return m;
    }

    public void exitRing() {
        log("Emitting leave token");
        Token exitToken = Token.newBuilder().setType(TokenType.EXIT).setEmitterId(id).setNext(next.toNodeData())
                .build();
        exiting = true;
        passNext(exitToken);
    }

    public void sendStatToGateway(StatUnitBean sb) {
        new Thread(() -> {
            WebTarget gatewayPath = ClientBuilder.newClient().target("http://localhost:1337/node/send_stats");
            Invocation.Builder invocationBuilder = gatewayPath.request(MediaType.APPLICATION_JSON);
            invocationBuilder.post(Entity.json(sb));
        }).start();
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