package node;

import java.util.concurrent.TimeUnit;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import node.GreetingServiceGrpc.GreetingServiceBlockingStub;
import node.GreetingServiceGrpc.GreetingServiceStub;
import node.GreetingServiceOuterClass.HelloRequest;
import node.GreetingServiceOuterClass.HelloResponse;

public class GreetingServerClient {
    public static void main(final String[] args) {
        // syncCall();
        System.out.println("Async");
        asyncCall();
    }

    public static void syncCall() {
        final Channel channel = ManagedChannelBuilder.forTarget("localhost:8080").usePlaintext(true).build();

        final GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

        final HelloRequest request = HelloRequest.newBuilder().setName("QuestionMario").build();

        final HelloResponse response = stub.greeting(request);

        System.out.println(response.getGreeting());

        ((ManagedChannel) channel).shutdown();

    }

    public static void asyncCall() {
        final Channel channel = ManagedChannelBuilder.forTarget("localhost:8080").usePlaintext(true).build();

        final GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);

        final HelloRequest request = HelloRequest.newBuilder().setName("LOL").build();

        stub.streamGreeting(request, new StreamObserver<HelloResponse>() {
            public void onNext(final HelloResponse response) {
                System.out.println(response.getGreeting());
            }

            @Override
            public void onError(final Throwable t) {
                System.out.println(t.getMessage());

            }

            @Override
            public void onCompleted() {
                ((ManagedChannel) channel).shutdown();

            }
        });
        try {
            ((ManagedChannel) channel).awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}