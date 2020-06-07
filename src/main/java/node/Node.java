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
    private String ip;
    private boolean exiting = false;
    private boolean isLettingNodeIn = false;
    private boolean hasEmittedToken = false;
    private List<NodeBean> nodeList;
    private Server nodeServer = null;
    private SlidingWindowBuffer buffer = new SlidingWindowBuffer();
    private PM10Simulator sensor;
    private String serverUrl;
    Object startLock = new Object();
    Object leaveLock = new Object();
    private boolean skippedLast = false;
    StreamObserver<Token> nextNodeHandler;
    ManagedChannel nextNodeChannel = null;

    public Node(int port, int id, String ip, String serverUrl, int serverPort) {
        this.port = port;
        this.id = id;
        this.ip = ip;
        this.serverUrl = "http://" + serverUrl + ":" + serverPort;
        this.next = this.toNodeBean();
    }

    public Node(int port, int id) {
        this(port, id, "localhost", "localhost", 1337);
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

    public boolean isExiting() {
        return exiting;
    }

    public boolean isLettingNodeIn() {
        return isLettingNodeIn;
    }

    public void setIsLettingNodeIn(boolean b) {
        isLettingNodeIn = b;
    }

    public void setNext(NodeBean newNext) {
        next = newNext;
        Thread channelThread = new Thread(() -> {
            openChannelWithNode(next);
            if (nodeList.size() == 1 && hasEmittedToken == false && nodeList.get(0).getId() == id) {
                hasEmittedToken = true;
                passNext(Token.newBuilder().setType(TokenType.DATA).build());
            }
        });
        channelThread.start();
    }

    public void startServer() {
        Runnable r = () -> {
            nodeServer = ServerBuilder.forPort(port).addService(new NodeServiceImpl(this)).build();
            try {
                nodeServer.start();
                log("** Server started");
                synchronized (startLock) {
                    startLock.notify();
                }
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

    public void showConsole() {
        new Thread(() -> {
            log("######################################");
            log("#######   SDP Project NODE  " + id + "  #######");
            log("#### Sebastiano Caccaro AA.18/19 #####");
            log("######################################");
            log("");
            log("Press ENTER at any moment to shutdown NODE" + id);
            log("-------------------------------------------");
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
            log("Shutting down the Node, please wait a moment :)");
            exitRing();
        }).start();
    }

    public void getNodeList() {
        WebTarget gatewayPath = ClientBuilder.newClient().target(serverUrl + "/node/join");
        Invocation.Builder invocationBuilder = gatewayPath.request(MediaType.APPLICATION_JSON);
        nodeList = invocationBuilder.post(Entity.json(this.toNodeBean()), NodeBeanList.class).getNodes();
        // MockServer ms = new MockServer();
        // nodeList = ms.register(this.toNodeBean());
        if (nodeList.size() > 1) {
            // Shuffling the node list in order to minimize multiple nodes aking to join
            // a single node at the same time
            nodeList.removeIf((NodeBean n) -> n.getId() == id);
            Collections.shuffle(nodeList);
        }
    }

    public void init() throws IOException, InterruptedException {
        startSensor();
        startServer();

        synchronized (startLock) {
            startLock.wait();
        }

        getNodeList();
        boolean canJoin = joinAfter(nodeList.get(0));
        int i = 1;
        while (!canJoin) {
            if (i < nodeList.size()) {
                canJoin = joinAfter(nodeList.get(i));
                i++;
            } else {
                unregisterFromGateway();
                getNodeList();
                i = 0;
            }
        }
        /* TODO: uncommente */
        // showConsole();
    }

    public boolean joinAfter(NodeBean nodeToAsk) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(nodeToAsk.fullAddresse()).usePlaintext(true).build();
        NodeServiceBlockingStub stub = NodeServiceGrpc.newBlockingStub(channel);
        JoinResponse response = stub.joinAfter(this.toNodeData());
        if (response.getJoinApproved()) {
            setNext(new NodeBean(response.getNextNode()));
        } else {
            return false;
        }
        channel.shutdown();
        return true;
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
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });

        setIsLettingNodeIn(false);
        log("Opened channel with N" + next.getId());
    }

    public void passNext(Token t) {
        nextNodeHandler.onNext(t);
    }

    public void unregisterFromGateway() {
        WebTarget gatewayPath = ClientBuilder.newClient().target(serverUrl + "/node/leave/" + id);
        Invocation.Builder invocationBuilder = gatewayPath.request(MediaType.APPLICATION_JSON);
        invocationBuilder.delete();
    }

    private HashMap<Long, Token> tokenQueue = new HashMap<Long, Token>();

    public void handleToken(Token t) throws InterruptedException {
        /*
         * The tokenqueue is needed to ensure the token are handled in a FIFO way, as
         * the sychronized statement does not guarantee ordering. This way, there won't
         * be overtaking between the tokens
         */
        synchronized (tokenQueue) {
            tokenQueue.put(new Date().getTime(), t);
        }

        synchronized (this) {
            synchronized (tokenQueue) {
                List<Long> keys = new LinkedList<Long>(tokenQueue.keySet());
                Collections.sort(keys);
                t = tokenQueue.remove(keys.get(keys.size() - 1));
                if (tokenQueue.size() == 0) {
                    tokenQueue.notify();
                }
            }
            switch (t.getType()) {
                case DATA:
                    log("Received data token");
                    if (!(exiting && id == next.getId())) {
                        t = handleAndGenerateDataToken(t);
                        passNext(t);
                    }
                    break;

                case EXIT:
                    log("Received exit token by N" + t.getEmitterId());
                    int emitterId = t.getEmitterId();
                    if (emitterId == id) {
                        synchronized (leaveLock) {
                            if (skippedLast == true) {
                                log("Waiting for skipped");
                                leaveLock.wait();
                            }
                        }
                        synchronized (tokenQueue) {
                            if (tokenQueue.size() > 0) {
                                log("Waiting for queue > " + tokenQueue.size());
                                tokenQueue.wait();
                            }

                        }
                        log("Getting out for good");
                        nextNodeHandler.onCompleted();
                        nextNodeChannel.shutdown();
                        nodeServer.shutdown();
                        sensor.stopMeGently();
                        unregisterFromGateway();
                        /* TODO: Uncomment */
                        // System.exit(0);
                    } else if (emitterId == next.getId()) {
                        passNext(t);
                        setNext(new NodeBean(t.getNext()));
                        /* TODO: check'n'reverse */
                    } else if (exiting && t.getNext().getId() == id) {
                        log("" + t);
                        t = t.toBuilder().setNext(next.toNodeData()).build();
                        passNext(t);
                        log("@@@@@" + t);
                    } else {
                        passNext(t);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //
    // The data token has 3 fields:
    // - Data: each node put here its local stat when compiled
    // - Skips: if a node hasn't computed its local stat yet, he must put its id
    // in this field. Note the node cannot leave the net if its id is in the
    // skips field
    // - Writes: when I write a stat, I also write my id in this list. Whenever
    // I receive a token with an empty skip list and my id in writes,
    // I contact the gateway
    //
    public Token handleAndGenerateDataToken(Token received) {
        List<Integer> written = new LinkedList<Integer>(received.getWritesList());
        List<Integer> skipped = new LinkedList<Integer>(received.getSkipsList());

        if (exiting && !skipped.contains(id)) {
            synchronized (leaveLock) {
                leaveLock.notify();
                skippedLast = false;
                return received;
            }
        }

        if (written.contains(id)) {
            synchronized (leaveLock) {
                skippedLast = false;
                leaveLock.notify();
                if (skipped.size() == 0) {
                    // Token is full: I send it and emit a new one
                    sendStatToGateway(mean(received.getStatList()));
                    return Token.newBuilder().setType(TokenType.DATA).build();
                } else {
                    // I don't have to do anything, I just pass on the token
                    return received;
                }
            }
        } else {
            if (buffer.isValueProduced()) {
                // I write down mu value, and remove my skip if its present
                synchronized (leaveLock) {
                    skippedLast = false;
                    leaveLock.notify();
                    skipped.remove(new Integer(id));
                    return received.toBuilder().addStat(buffer.getLastStat()).clearSkips().addAllSkips(skipped)
                            .addWrites(id).build();
                }

            } else {
                int skipIndex = skipped.indexOf(id);
                Builder b = received.toBuilder();
                if (skipIndex == -1) {
                    synchronized (leaveLock) {
                        b.addSkips(id);
                        skippedLast = true;
                    }
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
        Token exitToken = Token.newBuilder().setType(TokenType.EXIT).setEmitterId(id).setLastId(id)
                .setNext(next.toNodeData()).build();
        exiting = true;
        passNext(exitToken);
    }

    public void sendStatToGateway(StatUnitBean sb) {
        new Thread(() -> {
            WebTarget gatewayPath = ClientBuilder.newClient().target(serverUrl + "/node/send_stats");
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
            e.printStackTrace();
        }
    }

}