package recorder_server;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proto.RecordOuterClass;
import proto.RecorderGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;


/** Server that only accepts all sent data */
public class RecorderServer {
    private static final Logger logger = Logger.getLogger(RecorderServer.class.getName());

    private final int port;
    private final Server server;
    private final List<String> list = new ArrayList<>();

    public RecorderServer(int port) {
        this.port = port;
        server = ServerBuilder.forPort(port)
                .addService(new RecorderServiceImpl())
                .build();
    }

    public void start() throws Exception {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.err.println("*** server shut down");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    private class RecorderServiceImpl extends RecorderGrpc.RecorderImplBase {
        @Override
        public void receiveRecord(RecordOuterClass.Record request, StreamObserver<RecordOuterClass.Record> responseObserver) {
            list.add(request.getRecord());
            logger.info("added record to list, current size: " + list.size());

            RecordOuterClass.Record response = RecordOuterClass.Record.newBuilder()
                    .setRecord("OK")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws Exception {
        RecorderServer server = new RecorderServer(8980);
        server.start();
        server.blockUntilShutdown();
    }

}
