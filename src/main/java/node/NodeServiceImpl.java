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
        nodeRef.setNext(new NodeBean(joiningNode));
    }

    @Override
    public StreamObserver<Token> passNext(StreamObserver<ExitingResponse> response) {
        // TOOD: implement

        StreamObserver<Token> tokenStream = new StreamObserver<Token>() {

            @Override
            public void onNext(Token value) {
                nodeRef.handleToken(value);
            }

            @Override
            public void onError(Throwable t) {
                // TODO Auto-generated method stub
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // TODO Auto-generated method stub
            }

        };
        return tokenStream;
    }

}