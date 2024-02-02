package chord;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import proto.Chord;
import proto.ChordServiceGrpc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.int;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChordNode {
    private static final Logger logger = Logger.getLogger(ChordNode.class.getName());
    private NodeReference predecessor;
    private final NodeReference node;
    private NodeReference successor;
    private final HashMap<int, String> localData = new HashMap<>();
    private final ArrayList<NodeReference> fingerTable;

//    private static final int m = 160; // number of bits in id as well as max size of fingerTable
    private static final int m = 3; // 0-255 ids

    private final Server server;
    private ChordServiceGrpc.ChordServiceBlockingStub blockingStub;


    public ChordNode(String ip, int port) {
        this.node = new NodeReference(ip, port);
        this.predecessor = node;
        this.successor = node;
        fingerTable = new ArrayList<>(Collections.nCopies(m, node));

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



    public static int calculateSHA1(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            warning("Failed to calculate SHA-1 for input %s:%d", input);
            e.printStackTrace(System.err);
            return 0;
        }

        return new BigInteger(1,
                md.digest(input.getBytes(StandardCharsets.UTF_8)))
                .mod(BigInteger.valueOf(7L))// TODO: delete
                .intValue();
    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private static void debug(String msg, Object... params) {
        msg = "method: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " "+ msg;
        logger.log(Level.FINE, msg, params);
    }

    private static void trace(String msg, Object... params) {
        msg = "method: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " "+ msg;
        logger.log(Level.FINEST, msg, params);
    }

    private static void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }

    public static class NodeReference {
        public final String ip;
        public final int port;
        public int id;
        public int end;

        public NodeReference(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.id = calculateSHA1(this.toString());
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
        // TODO: update_others()
    }

    private void initFingerTable(NodeReference bootstrapNode) {
        // kdyz X joinuje, pta se bootstrapNode aby nasel jeho primej successor
        int myId = node.id;
        NodeReference immediateSuccessor = findSuccessor(myId);
        fingerTable.set(0, immediateSuccessor);
        this.predecessor = immediateSuccessor;
        // TODO
    }

    // bootstrap join
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

    public NodeReference findSuccessor(int id) {
        NodeReference n_ = findPredecessor(id);
        // TODO return successor_of(n_);
        return null;
    }

    public NodeReference findPredecessor(int id) {
        // contacts series of nodes moving forward around the Chord towards id
        NodeReference n_ = this.node;
        while (!isOnArch(id, n_.id, n_.end)) {
            // n asks n_ for the node n_ knows about that most closely precedes id => grpc
            n_ = closestPrecedingFingerOf(n_, id);
        }
        return n_;
    }

    // GRPC call
    public NodeReference closestPrecedingFingerOf(NodeReference n_, int id) {
        // TODO: continue here

    }

    public static boolean isOnArch(int id, int start, int end) {
        // Wrap around arch; id ∈ [start, 2^m) || id ∈ [0, end)
        if (start > end) { // Wrap around
            if (id >= start)
                return true;
            if (id >= 0 && id < end)
                return true;
            return false;
        }
        return id >= start && id < end;
    }



    /** Procedures served to other nodes */
    private class ChordServiceImpl extends ChordServiceGrpc.ChordServiceImplBase {

//        @Override
//        public void findSuccessor(Chord.FindSuccessorRequest request, StreamObserver<Chord.FindSuccessorResponse> responseObserver) {
//            NodeReference currSuccessor;
//            int queriedId;
//            try {
//                queriedId = calculateSHA1(request.getPrecedingId());
//            } catch (NoSuchAlgorithmException e) {
//                throw new RuntimeException(e);
//            }
//
//            currSuccessor = node;
//            int i = m-1;
//            while (!isOnArch(queriedId, node.id, node.end)) {
//                currSuccessor = fingerTable.get(i);
//                i--;
//            }
//
//            Chord.FindSuccessorResponse response = Chord.FindSuccessorResponse.newBuilder()
//                    .setRequestorIp(request.getSenderIp())
//                    .setRequestorPort(request.getSenderPort())
//                    .setSuccessorIp(currSuccessor.ip)
//                    .setSuccessorPort(currSuccessor.port)
//                    .build();
//
//            responseObserver.onNext(response);
//            responseObserver.onCompleted();
//        }

        public void closestPrecedingFinger(String id) {
            calculateSHA1(id)
            for (int i = m-1; i >= 0; i--) {
                int currId = fingerTable.get(i).id;
                if (isOnArch(currId, node.id, id))
            }
        }

        // Called on A from X
        @Override
        public void join(Chord.JoinRequest requestFromNodeX, StreamObserver<Chord.JoinResponse> responseObserver) {
            NodeReference newNode = new NodeReference(requestFromNodeX.getSenderIp(), requestFromNodeX.getSenderPort());

            int id = calculateSHA1(newNode.ip + ":" + newNode.port);

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
