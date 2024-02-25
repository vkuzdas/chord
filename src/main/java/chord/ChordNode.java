package chord;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Chord;
import proto.ChordServiceGrpc;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.pow;

public class ChordNode {
    private static final Logger logger = LoggerFactory.getLogger(ChordNode.class);
    private NodeReference predecessor;
    private final NodeReference node;
    private NodeReference successor;
    private final NavigableMap<Integer, String> localData = new TreeMap<>();
    private final ArrayList<NodeReference> fingerTable;
    private boolean isAlone = true;

//    private static final int m = 160; // TODO: number of bits in id as well as max size of fingerTable
    private static final int m = 4; // 0-255 ids

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
        logger.debug("Server started, listening on {}", node.port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("*** shutting down gRPC server since JVM is shutting down");
            try {
                stop();
            } catch (InterruptedException e) {
                logger.warn("*** server shut down interrupted");
                e.printStackTrace(System.err);
            }
            logger.warn("*** server shut down");
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
            logger.warn("Failed to calculate SHA-1 for input %s", input);
            e.printStackTrace(System.err);
            return 0;
        }

        return new BigInteger(1,
                md.digest(input.getBytes(StandardCharsets.UTF_8)))
                .mod(BigInteger.valueOf((long)pow(2,m)-1))
                .intValue();
    }


    public static class NodeReference {
        public final String ip;
        public final int port;
        public int id;

        public NodeReference(String ip, int port) {
            this.ip = ip;
            this.port = port;
            this.id = calculateSHA1(this.toString());
        }

        @Override
        public String toString() {
            return ip + ":" + port;
        }

        @Override
        public boolean equals(Object obj) {
            NodeReference other = (NodeReference) obj;
            return this.port == other.port && this.ip.equals(other.ip);
        }
    }

    // Called on X from client
    public void join(NodeReference n_) {
        initFingerTable(n_);
        updateOthers();
//        moveKeys_RPC(); //from the range (predecessor,n] from successor
    }

    private void moveKeys_RPC() {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.MoveKeysRequest request = Chord.MoveKeysRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setRangeStart(predecessor.id)
                .setRangeEnd(node.id)
                .build();
        Chord.MoveKeysResponse response = blockingStub.moveKeys(request);
        int cnt = response.getValueCount();
        for (int i = 0; i < cnt; i++) {
            String value = response.getValue(i);
            int key = response.getKey(i);
            localData.put(key, value);
        }
        channel.shutdown();
    }

    private void updateOthers() {
        for (int i = 0; i < m; i++) {
            int id = node.id - (int)pow(2, i-1);
            if (id<0) { // wraparound
                id = id + (int)pow(2, m);
            }
            NodeReference p = findPredecessor(id);
            logger.debug("updateOthers: pred of {} is {}, i={}", id, p, i);
            updateFingerTableOf_RPC(p, node, i);
        }
    }

    private void updateFingerTableOf_RPC(NodeReference p, NodeReference s, int index) {
        Chord.UpdateFingerTableRequest req = Chord.UpdateFingerTableRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setIndex(index)
                .setNodeIp(s.ip)
                .setNodePort(s.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(p.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdateFingerTableResponse response = blockingStub.updateFingerTable(req);
        channel.shutdown();
    }


    private void initFingerTable(NodeReference n_) {
        int targetId = fingerTable.get(0).id;
        this.successor = findSuccessor_RPC(n_, targetId);
        fingerTable.set(0, this.successor);
        this.predecessor = getPredecessor_RPC(this.successor);
        for (int i = 0; i < m-1; i++) {
            NodeReference curr = fingerTable.get(i);
            NodeReference next = fingerTable.get(i+1);
            if (isBetweenExclusive(next.id, node.id, curr.id) || next.id==node.id) {
                fingerTable.set(i+1, curr);
            } else {
                fingerTable.set(i+1, findSuccessor_RPC(n_, next.id));
            }
        }
    }

    public NodeReference getPredecessor_RPC(NodeReference targetNode) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetNode.toString()).usePlaintext().build();
        Chord.GetPredecessorRequest request = Chord.GetPredecessorRequest.newBuilder()
                .setRequestorIp(this.node.ip)
                .setRequestorPort(this.node.port)
                .build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);

        Chord.GetPredecessorResponse response = blockingStub.getPredecessor(request);
        channel.shutdown();
        return new NodeReference(response.getPredecessorIp(), response.getPredecessorPort());
    }

    public NodeReference findSuccessor_RPC(NodeReference n_, int targetId) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.toString()).usePlaintext().build();
        Chord.FindSuccessorRequest request = Chord.FindSuccessorRequest.newBuilder()
                .setSenderIp(this.node.ip)
                .setSenderPort(this.node.port)
                .setTargetId(targetId)
                .build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);

        Chord.FindSuccessorResponse response = blockingStub.findSuccessor(request);
        channel.shutdown();
        return new NodeReference(response.getSuccessorIp(), response.getSuccessorPort());
    }

    public NodeReference findSuccessor(int id) {
        NodeReference n_ = findPredecessor(id);
        return getSuccessor_RPC(n_);
    }

    public NodeReference getSuccessor_RPC(NodeReference n_) {
        if (n_.equals(node)) {
            return successor;
        }
        Chord.GetSuccessorRequest req = Chord.GetSuccessorRequest
                .newBuilder()
                .setRequestorIp(this.node.ip)
                .setRequestorPort(this.node.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.GetSuccessorResponse response = blockingStub.getSuccessor(req);
        channel.shutdown();
        return new NodeReference(response.getSuccessorIp(), response.getSuccessorPort());
    }

    public NodeReference findPredecessor(int id) {
        // contacts series of nodes moving forward around the Chord towards id
        if (id == node.id) {
            return this.predecessor;
        }
        NodeReference n_ = this.node;
        NodeReference S = getSuccessor_RPC(n_);
        if (n_.equals(S)) { // when there is only bootstrap in the network
            return n_;
        }
         while ( !(isBetweenExclusive(id, n_.id, S.id) || id==S.id) ) {
            n_ = closestPrecedingFingerOf(n_, id);
            S = getSuccessor_RPC(n_);
        }
        return n_;
    }

    // GRPC call
    public NodeReference closestPrecedingFingerOf(NodeReference n_, int id) {
        // send request to node n_ with id
        if (n_.equals(node)) {
            for (int i = m-1; i >= 0; i--) {
                NodeReference curr = fingerTable.get(i);
                if (isBetweenExclusive(curr.id, node.id, id)) {
                    return curr;
                }
            }
            return this.node;
        } else {
            return closestPrecedingFinger_RPC(n_, id);
        }
    }

    private NodeReference closestPrecedingFinger_RPC(NodeReference n_, int id) {
        Chord.ClosestPrecedingFingerRequest cpfr = Chord.ClosestPrecedingFingerRequest
                .newBuilder()
                .setTargetId(id)
                .setSenderIp(this.node.ip)
                .setSenderPort(this.node.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.ip + ":" + n_.port).usePlaintext().build();
        logger.debug("[{}] asking node [{}] for closestPrecedingFinger of id={}", node, n_, id);
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.ClosestPrecedingFingerResponse response = blockingStub.closestPrecedingFinger(cpfr);
        return new NodeReference(response.getClosestPrecedingFingerIp(), response.getClosestPrecedingFingerPort());
    }

    public static boolean isBetweenExclusive(int id, int start, int end) {
        // Wrap around arch; id ∈ [start, 2^m) || id ∈ [0, end)
        if (start > end) { // Wrap around
            if (id > start)
                return true;
            if (id > 0 && id < end)
                return true;
            return false;
        }
        return id > start && id < end;
    }



    /** Procedures served to other nodes */
    private class ChordServiceImpl extends ChordServiceGrpc.ChordServiceImplBase {

        // invokes findSuccessor procedure
        @Override
        public void findSuccessor(Chord.FindSuccessorRequest request, StreamObserver<Chord.FindSuccessorResponse> responseObserver) {
            int targetId = request.getTargetId();

            NodeReference n = ChordNode.this.findSuccessor(targetId);
            if (isAlone) { // TODO: set true when all nodes leave/shutdown
                NodeReference joiningNode = new NodeReference(request.getSenderIp(), request.getSenderPort());
                successor = joiningNode;
                fingerTable.set(0, joiningNode);
                isAlone = false;
            }

            Chord.FindSuccessorResponse r = Chord.FindSuccessorResponse.newBuilder()
                    .setSuccessorIp(n.ip)
                    .setSuccessorPort(n.port)
                    .build();
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }

        // return this.successor
        @Override
        public void getSuccessor(Chord.GetSuccessorRequest request, StreamObserver<Chord.GetSuccessorResponse> responseObserver) {
            Chord.GetSuccessorResponse response = Chord.GetSuccessorResponse.newBuilder()
                    .setSuccessorIp(successor.ip)
                    .setSuccessorPort(successor.port)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        // return this.predecessor
        @Override
        public void getPredecessor(Chord.GetPredecessorRequest request, StreamObserver<Chord.GetPredecessorResponse> responseObserver) {
            Chord.GetPredecessorResponse response = Chord.GetPredecessorResponse.newBuilder()
                    .setPredecessorIp(predecessor.ip)
                    .setPredecessorPort(predecessor.port)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void updateFingerTable(Chord.UpdateFingerTableRequest request, StreamObserver<Chord.UpdateFingerTableResponse> responseObserver) {
            int index = request.getIndex();
            NodeReference s = new NodeReference(request.getNodeIp(), request.getNodePort());
            NodeReference i = fingerTable.get(index);
            if (isBetweenExclusive(s.id, node.id, i.id) || s.id==node.id) {
                fingerTable.set(index, s);
                updateFingerTableOf_RPC(predecessor, s, index);
            }
            Chord.UpdateFingerTableResponse response = Chord.UpdateFingerTableResponse.newBuilder()
                    .setStatus("OK")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void closestPrecedingFinger(Chord.ClosestPrecedingFingerRequest request, StreamObserver<Chord.ClosestPrecedingFingerResponse> responseObserver) {
            int id = request.getTargetId();
            for (int i = m-1; i >= 0; i--) {
                NodeReference curr = fingerTable.get(i);
                if (isBetweenExclusive(curr.id, node.id, id) || curr.id==node.id) {
                    Chord.ClosestPrecedingFingerResponse r = Chord.ClosestPrecedingFingerResponse.newBuilder()
                            .setClosestPrecedingFingerIp(curr.ip)
                            .setClosestPrecedingFingerPort(curr.port)
                            .build();
                    responseObserver.onNext(r);
                    responseObserver.onCompleted();
                    return;
                }
            }
            Chord.ClosestPrecedingFingerResponse r = Chord.ClosestPrecedingFingerResponse.newBuilder()
                    .setClosestPrecedingFingerIp(node.ip)
                    .setClosestPrecedingFingerPort(node.port)
                    .build();
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }

        @Override
        public void moveKeys(Chord.MoveKeysRequest request, StreamObserver<Chord.MoveKeysResponse> responseObserver) {
            Chord.MoveKeysResponse.Builder response = Chord.MoveKeysResponse.newBuilder();
            int start = request.getRangeStart();
            int end = request.getRangeEnd();
            // range = (start, end]
            localData.subMap(start+1, end+1).forEach((k, v) -> {
                response.addKey(k);
                response.addValue(v);
                localData.remove(k);
            });
            responseObserver.onNext(response.build());
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
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        bootstrap.start();
        node2.start();

        node2.join(bootstrap.node);

        bootstrap.blockUntilShutdown();
        node2.blockUntilShutdown();


//        String input;
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        while (true) {
//            System.out.println("Enter 'join <ip>:<port>' to join network");
//            System.out.println("Enter 'run' to start the node");
//            System.out.println("Enter 'exit' to quit");
//            System.out.println("Enter 'put <key> <value>' to put a record");
//            System.out.println("Enter 'get <key>' to get a record");
//            System.out.println("Enter 'print' to print finger table");
//            System.out.print("Enter your message: ");
//            input = reader.readLine();
//            String[] tokens = input.split("\\s+");
//            if ("EXIT".equalsIgnoreCase(tokens[0])) {
//                System.exit(0);
//            }
//            if ("RUN".equalsIgnoreCase(tokens[0])) {
//                break;
//            }
//            if ("JOIN".equalsIgnoreCase(tokens[0])) {
//                String ip = tokens[1].split(":")[0];
//                String port = tokens[1].split(":")[1];
//                NodeReference bootstrap = new NodeReference(ip, Integer.parseInt(port));
//                node.join(bootstrap);
//            }
//        }

    }
}
