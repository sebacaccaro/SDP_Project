package node;

import io.grpc.stub.StreamObserver;
import node.GreetingServiceGrpc.GreetingServiceImplBase;
import node.GreetingServiceOuterClass.HelloRequest;
import node.GreetingServiceOuterClass.HelloResponse;

public class GreetingServiceImpl extends GreetingServiceImplBase {

    @Override
    public void greeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.print("Richiesta arrivata");
        System.out.print(request);

        HelloResponse response = HelloResponse.newBuilder().setGreeting("Bella zio, " + request.getName()).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void streamGreeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        System.out.print("Richiesta arrivata");
        System.out.print(request);

        HelloResponse response = HelloResponse.newBuilder().setGreeting("Bella zio, " + request.getName()).build();

        responseObserver.onNext(response);
        responseObserver.onNext(response);
        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

}