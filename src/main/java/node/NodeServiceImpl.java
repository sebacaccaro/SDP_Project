package node;

import gateway.store.beans.NodeBean;
import io.grpc.internal.Stream;
import io.grpc.stub.StreamObserver;
import node.JoinService.ExitingResponse;
import node.JoinService.JoinResponse;
import node.JoinService.Token;
import node.NodeDataOuterClass.NodeData;
import node.NodeServiceGrpc.NodeServiceImplBase;

public class NodeServiceImpl extends NodeServiceImplBase {

    private Node nodeRef;

    public NodeServiceImpl(Node n) {
        nodeRef = n;
    }

    /**
     * The server must set the joining node as next, and respond with its current
     * next wrapped in a JoinResponse
     */
    @Override
    public void joinAfter(NodeData joiningNode, StreamObserver<JoinResponse> responseStream) {
        // TODO: also condition if node cannot join
        JoinResponse response = JoinResponse.newBuilder().setJoinApproved(true)
                .setNextNode(nodeRef.getNext().toNodeData()).build();
        responseStream.onNext(response);
        responseStream.onCompleted();
        nodeRef.log("SetNext Called From server to " + new NodeBean(joiningNode));
        if (joiningNode.getId() != nodeRef.toNodeBean().getId()) {
            nodeRef.setNext(new NodeBean(joiningNode));
        }
    }

    @Override
    public StreamObserver<Token> passNext(StreamObserver<ExitingResponse> response) {
        // TOOD: implement
        nodeRef.log("Started passnext server");

        StreamObserver<Token> tokenStream = new StreamObserver<Token>() {

            @Override
            public void onNext(Token value) {
                nodeRef.handleToken(value);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                nodeRef.log("Connection Terminated");
            }

        };
        return tokenStream;
    }

}