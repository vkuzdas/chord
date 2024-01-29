package chord;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proto.Chord;
import proto.ChordServiceGrpc;
import proto.RecordOuterClass;
import proto.RecorderGrpc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordNode {
    private static final Logger logger = Logger.getLogger(ChordNode.class.getName());

    private NodeReference predecessor;
    private final NodeReference node;
    private NodeReference successor;
    private final HashMap<BigInteger, String> localData = new HashMap<>();
    private final ArrayList<NodeReference> fingerTable = new ArrayList<>(m);

    private static final int m = 160; // number of bits in id as well as max size of fingerTable

    private final Server server;
    private ChordServiceGrpc.ChordServiceBlockingStub blockingStub;


    public ChordNode(String ip, int port) {
        this.node = new NodeReference(ip, port);
        this.predecessor = node;
        this.successor = node;

        server = ServerBuilder.forPort(port)
                .addService(new ChordNode.ChordServiceImpl())
                .build();
    }

    public void start() throws Exception {
        server.start();
        logger.info("Server started, listening on " + node.port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warning("*** shutting down gRPC server since JVM is shutting down");
            try {
                stop();
            } catch (InterruptedException e) {
                logger.warning("*** server shut down interrupted");
                e.printStackTrace(System.err);
            }
            logger.warning("*** server shut down");
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



    public static BigInteger calculateSHA1(String input) throws NoSuchAlgorithmException {
        return new BigInteger(1,
                MessageDigest.getInstance("SHA-1").digest(input.getBytes(StandardCharsets.UTF_8))
        );
    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private void debug(String msg, Object... params) {
        msg = "method: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " "+ msg;
        logger.log(Level.FINE, msg, params);
    }

    private void trace(String msg, Object... params) {
        msg = "method: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " "+ msg;
        logger.log(Level.FINEST, msg, params);
    }

    private void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }

    public static class NodeReference {
        public final String ip;
        public final int port;

        public NodeReference(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public String toString() {
            return ip + port;
        }
    }

    // Called on X from client
    public void join(NodeReference nodeA) {
        // Initializing fingers and predecessor
        initFingerTable(nodeA);
        /** TODO:
         *      P = S.P
         *      S.P = this.node
         *      1..m-1: finger[i+1]     */
    }

    private void initFingerTable(NodeReference bootstrapNode) {
        // kdyz X joinuje, pta se bootstrapNode aby nasel jeho primej successor
        String myId = node.toString();
        NodeReference immediateSuccessor = findSuccessor(bootstrapNode, myId);
        fingerTable.set(0, immediateSuccessor);
    }


    // TODO: implement server side
    public NodeReference findSuccessor(NodeReference askedNode, String precedingId) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(askedNode.ip + ":" + askedNode.port).usePlaintext().build();
        Chord.FindSuccessorRequest request = Chord.FindSuccessorRequest.newBuilder()
                .setSenderIp(this.node.ip)
                .setSenderPort(this.node.port)
                .setPrecedingId(precedingId)
                .build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);

        Chord.FindSuccessorResponse response = blockingStub.findSuccessor(request);
        return new NodeReference(response.getSuccessorIp(), response.getSuccessorPort());
    }


    private class ChordServiceImpl extends ChordServiceGrpc.ChordServiceImplBase {

        @Override
        public void findSuccessor(Chord.FindSuccessorRequest request, StreamObserver<Chord.FindSuccessorResponse> responseObserver) {

        }

        // Called on A from X
        @Override
        public void join(Chord.JoinRequest requestFromNodeX, StreamObserver<Chord.JoinResponse> responseObserver) {
            NodeReference newNode = new NodeReference(requestFromNodeX.getSenderIp(), requestFromNodeX.getSenderPort());




            try {
                BigInteger id = calculateSHA1(newNode.ip + ":" + newNode.port);

            } catch (NoSuchAlgorithmException e) {
                warning("Failed to calculate SHA-1 for node %s:%d", newNode.ip, newNode.port);
                e.printStackTrace(System.err);
            }
            Chord.JoinResponse response = Chord.JoinResponse.newBuilder()
                    .setSenderIp(node.ip)
                    .setSenderPort(node.port)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void put(Chord.PutRequest request, StreamObserver<Chord.PutResponse> responseObserver) {
            super.put(request, responseObserver);
        }

        @Override
        public void get(Chord.GetRequest request, StreamObserver<Chord.GetResponse> responseObserver) {
            super.get(request, responseObserver);
        }
    }

    public static void main(String[] args) throws Exception {
        // start node
        ChordNode node = new ChordNode("localhost", 8980);
        node.start();

        String input;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Enter 'join <ip>:<port>' to join network");
            System.out.println("Enter 'run' to start the node");
            System.out.println("Enter 'exit' to quit");
            System.out.println("Enter 'put <key> <value>' to put a record");
            System.out.println("Enter 'get <key>' to get a record");
            System.out.println("Enter 'print' to print finger table");
            System.out.print("Enter your message: ");
            input = reader.readLine();
            String[] tokens = input.split("\\s+");
            if ("EXIT".equalsIgnoreCase(tokens[0])) {
                System.exit(0);
            }
            if ("RUN".equalsIgnoreCase(tokens[0])) {
                break;
            }
            if ("JOIN".equalsIgnoreCase(tokens[0])) {
                String ip = tokens[1].split(":")[0];
                String port = tokens[1].split(":")[1];
                NodeReference bootstrap = new NodeReference(ip, Integer.parseInt(port));
                node.join(bootstrap);
            }
        }

        node.blockUntilShutdown();
    }
}
