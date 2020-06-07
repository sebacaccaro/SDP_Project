package node;

import gateway.store.beans.NodeBean;
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
        if (nodeRef.isExiting() || nodeRef.isLettingNodeIn()) {
            JoinResponse response = JoinResponse.newBuilder().setJoinApproved(false).build();
            responseStream.onNext(response);
            responseStream.onCompleted();
        } else {
            nodeRef.setIsLettingNodeIn(true);
            JoinResponse response = JoinResponse.newBuilder().setJoinApproved(true)
                    .setNextNode(nodeRef.getNext().toNodeData()).build();
            responseStream.onNext(response);
            responseStream.onCompleted();
            if (joiningNode.getId() != nodeRef.toNodeBean().getId()) {
                nodeRef.setNext(new NodeBean(joiningNode));
            }
        }
    }

    @Override
    public StreamObserver<Token> passNext(StreamObserver<ExitingResponse> response) {

        StreamObserver<Token> tokenStream = new StreamObserver<Token>() {

            @Override
            public void onNext(Token value) {
                try {
                    nodeRef.handleToken(value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }

        };
        return tokenStream;
    }

}