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

import java.util.*;
import java.util.concurrent.TimeUnit;

import static chord.Util.*;
import static java.lang.Math.pow;

public class ChordNode {
    private static final Logger logger = LoggerFactory.getLogger(ChordNode.class);
    private NodeReference predecessor;
    private final NodeReference node;
    private final NavigableMap<Integer, String> localData = new TreeMap<>();
    private final ArrayList<Finger> fingerTable = new ArrayList<>();
    private boolean isAlone = true;
    public static int CHECK_INTERVAL = 2500;
    private Timer stabilizationTimer;

//    private static final int m = 160; // TODO: number of bits in id as well as max size of fingerTable
    public static final int m = 4; // 0-255 ids

    private final Server server;
    private ChordServiceGrpc.ChordServiceBlockingStub blockingStub;

    public NodeReference getNodeReference() {
        return node;
    }

    public NodeReference getPredecessor() {
        return predecessor;
    }

    public NodeReference getSuccessor() {
        return fingerTable.get(0).node;
    }

    public ChordNode(String ip, int port) {
        this.node = new NodeReference(ip, port);
        this.predecessor = node;

        for (int i = 1; i <= m; i++) {
            int start = (node.id + (int)pow(2, i-1)) % (int)pow(2, m);
            int end = (node.id + (int)pow(2, i) - 1) % (int)pow(2, m);
            fingerTable.add(new Finger(start, end, node));
        }

        server = ServerBuilder.forPort(port)
                .addService(new ChordNode.ChordServiceImpl())
                .build();
    }

    public void startServer() throws Exception {
        server.start();
        logger.trace("Server started, listening on {}", node.port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.warn("*** shutting down gRPC server on {} since JVM is shutting down", this.node);
            try {
                stopServer();
            } catch (InterruptedException e) {
                logger.warn("*** server shut down interrupted");
                e.printStackTrace(System.err);
            }
            logger.warn("*** server shut down on {}", this.node);
        }));
    }

    public void stopServer() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public int getDataSize() {
        return localData.size();
    }


    public void put(String key, String value) {
        int id = calculateSHA1(key, m);
        NodeReference n_ = findSuccessor(id);
        if (n_.equals(node)) {
            localData.put(id, value);
            logger.debug("<{},{}> saved on {}", id, value, node.id);
        } else {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.toString()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(ManagedChannelBuilder.forTarget(n_.toString()).usePlaintext().build());
            Chord.PutRequest request = Chord.PutRequest.newBuilder()
                    .setKey(key)
                    .setId(calculateSHA1(key, m))
                    .setValue(value)
                    .build();
            blockingStub.put(request);
            channel.shutdown();
        }
    }

    public String get(String key) {
        int id = calculateSHA1(key, m);
        NodeReference n_ = findSuccessor(id);
        if (n_.equals(node)) {
            String value = localData.get(id);
            logger.debug("n:{} returning <{},{}>", node.id, id, value);
            return value;
        } else {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.toString()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
            Chord.GetRequest request = Chord.GetRequest.newBuilder()
                    .setId(id)
                    .build();
            Chord.GetResponse response = blockingStub.get(request);
            channel.shutdown();
            return response.getValue();
        }
    }

    // Called on X from client
    public void join(ChordNode n_) {
        initFingerTable(n_.node);
        updateOthers();
        moveKeys_RPC(); //from the range (predecessor,n] from successor
        startFixThread();
    }

    public void leave() {
        // no need to delete n from FingerTables since periodic fix_fingers will do it
        stopFixThread();

        updateNeighbors();
        if(!localData.isEmpty())
            moveKeysToSuccessor_RPC();

        logger.debug("Node [{}:{}] left the network", node, node.id);
    }

    private void updateNeighbors() {
        // TODO: should I notify them to stop stabilize for a while?, maybe stopping timer for a while
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdateSuccessorRequest usr = Chord.UpdateSuccessorRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setSuccessorIp(successor.ip)
                .setSuccessorPort(successor.port)
                .build();
        blockingStub.updateSuccessor(usr);
        channel.shutdown();

        NodeReference predecessor = this.predecessor;
        channel = ManagedChannelBuilder.forTarget(predecessor.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdatePredecessorRequest upr = Chord.UpdatePredecessorRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setPredecessorIp(predecessor.ip)
                .setPredecessorPort(predecessor.port)
                .build();
        blockingStub.updatePredecessor(upr);
        channel.shutdown();
    }

    // all nodes from n are moved to n.successor
    private void moveKeysToSuccessor_RPC() {
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.MoveKeysToSuccessorRequest.Builder request = Chord.MoveKeysToSuccessorRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port);
        localData.forEach((k, v) -> {
            request.addKey(k);
            request.addValue(v);
        });
        blockingStub.moveKeysToSuccessor(request.build());
        channel.shutdown();
    }

    private void printStatus() {
        logger.debug("[{}:{}] P={}, S={}", node, node.id, predecessor, fingerTable.get(0).node);
    }

    private void startFixThread() {
        // periodic stabilization
        stabilizationTimer = new Timer();
        stabilizationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                fix_fingers();
                stabilize();
                printStatus();
            }
        },1000, CHECK_INTERVAL);
    }

    private void stopFixThread() {
        if (stabilizationTimer != null) {
            stabilizationTimer.cancel();
        }
    }

    private void fix_fingers() {
        int i = new Random().nextInt(m-1)+1;
        fingerTable.get(i).setNode(findSuccessor(fingerTable.get(i).start));
    }

    private void stabilize() {
        // ask S for S.P, decide whether to set n.P = S.P instead
        NodeReference s = fingerTable.get(0).node;
        NodeReference s_p = getPredecessor_RPC(s);
        logger.debug("[{}:{}]  s_p {}:{} ?E ({}, {})", node, node.id, s_p, s_p.id, node.id, s.id);
        if (inRange_OpenOpen(s_p.id, node.id, s.id)) {
            fingerTable.get(0).setNode(s_p);
        }
        // notify n.S of n's existence
        logger.debug("[{}:{}] existence notified to [{}:{}]", node, node.id, s, s.id);
        ManagedChannel channel = ManagedChannelBuilder.forTarget(s.toString()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.Notification notification = Chord.Notification.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setId(node.id)
                .build();
        blockingStub.notify(notification);
        channel.shutdown();
    }

    private void moveKeys_RPC() {
        NodeReference successor = fingerTable.get(0).node;
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

    private void updateOthers() { // updatuje itej finger uzlu p
        // update ith finger of p node
        for (int i = 0; i < m; i++) {
            int id = node.id - (int)pow(2, i-1);
            if (id < 0) { // wraparound
                id = id + (int)pow(2, m);
            }
            NodeReference p = findPredecessor(id);
            logger.debug("ID={} pred of {} is {}:{}, i={}", node.id, id, p, calculateSHA1(p.toString(), m), i);
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
        int targetId = fingerTable.get(0).start;
        NodeReference S = findSuccessor_RPC(n_, targetId);
        int sse = node.id+1 % (int)pow(2, m);
        fingerTable.set(0, new Finger(sse, sse+1, S)); // the first finger

        this.predecessor = getPredecessor_RPC(fingerTable.get(0).node);
        for (int i = 0; i < m-1; i++) {
            Finger curr = fingerTable.get(i);
            Finger next = fingerTable.get(i+1);
            if (inRange_CloseOpen(next.start, node.id, curr.node.id)) {
                fingerTable.get(i+1).setNode(curr.node);
            } else {
                NodeReference n_s = findSuccessor_RPC(n_, next.start);
                fingerTable.get(i+1).setNode(n_s);
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
            return fingerTable.get(0).node;
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
        while (!inRange_OpenClose(id, n_.id, S.id)) {
            logger.debug("ID={} findPredecessor: {} !E [{}, {})", node.id, id, n_.id, S.id);
            n_ = closestPrecedingFingerOf(n_, id);
            S = getSuccessor_RPC(n_);
        }
//        logger.debug("findPredecessor: {} E [{}, {})", id, n_.id, S.id);
        return n_;
    }

    // GRPC call
    public NodeReference closestPrecedingFingerOf(NodeReference n_, int id) {
        // send request to node n_ with id
        if (n_.equals(node)) {
            for (int i = m-1; i >= 0; i--) {
                Finger curr = fingerTable.get(i);
                if (inRange_OpenOpen(curr.node.id, node.id, id)) {
                    return curr.node;
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
        NodeReference cpf = new NodeReference(response.getClosestPrecedingFingerIp(), response.getClosestPrecedingFingerPort());
        logger.debug("[{}] got {}", node, cpf);
        return cpf;
    }




    /** Procedures served to other nodes */
    private class ChordServiceImpl extends ChordServiceGrpc.ChordServiceImplBase {

        // invokes findSuccessor procedure
        @Override
        public void findSuccessor(Chord.FindSuccessorRequest request, StreamObserver<Chord.FindSuccessorResponse> responseObserver) {
            int targetId = request.getTargetId();
            NodeReference n = ChordNode.this.findSuccessor(targetId);

            Chord.FindSuccessorResponse r = Chord.FindSuccessorResponse.newBuilder()
                    .setSuccessorIp(n.ip)
                    .setSuccessorPort(n.port)
                    .build();
            responseObserver.onNext(r);
            responseObserver.onCompleted();

            if (isAlone) { // TODO: set true when all nodes leave/shutdown
                NodeReference joiningNode = new NodeReference(request.getSenderIp(), request.getSenderPort());
                fingerTable.get(0).setNode(joiningNode);
                isAlone = false;
                startFixThread();
            }
        }

        // return this.successor
        @Override
        public void getSuccessor(Chord.GetSuccessorRequest request, StreamObserver<Chord.GetSuccessorResponse> responseObserver) {
            NodeReference successor = fingerTable.get(0).node;
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
            NodeReference i = fingerTable.get(index).node;
            if (inRange_CloseOpen(s.id, node.id, i.id)) {
                fingerTable.get(index).setNode(s);
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
                NodeReference curr = fingerTable.get(i).node;
                logger.debug("[{}]   {} e ({}, {})", node, curr.id, node.id, id);
                if (inRange_OpenOpen(curr.id, node.id, id)) {
                    Chord.ClosestPrecedingFingerResponse r = Chord.ClosestPrecedingFingerResponse.newBuilder()
                            .setClosestPrecedingFingerIp(curr.ip)
                            .setClosestPrecedingFingerPort(curr.port)
                            .build();
                    logger.debug("[{}] responding with cpf of [{}] ", node, curr);
                    responseObserver.onNext(r);
                    responseObserver.onCompleted();
                    return;
                }
            }
            Chord.ClosestPrecedingFingerResponse r = Chord.ClosestPrecedingFingerResponse.newBuilder()
                    .setClosestPrecedingFingerIp(node.ip)
                    .setClosestPrecedingFingerPort(node.port)
                    .build();
            logger.debug("[{}] responding with cpf of [{}] ", node, node);
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }

        @Override
        public void moveKeys(Chord.MoveKeysRequest request, StreamObserver<Chord.MoveKeysResponse> responseObserver) {
            Chord.MoveKeysResponse.Builder response = Chord.MoveKeysResponse.newBuilder();
            int start = request.getRangeStart();
            int end = request.getRangeEnd();
            // range = (start, end]
            if (!localData.isEmpty()) {
                if (start < end) {
                    localData.subMap(start+1, end+1).forEach((k, v) -> {
                        response.addKey(k);
                        response.addValue(v);
                        localData.remove(k);
                    });
                } else {
                    localData.subMap(start+1, (int)pow(2, m)).forEach((k, v) -> {
                        response.addKey(k);
                        response.addValue(v);
                        localData.remove(k);
                    });
                    localData.subMap(0, end+1).forEach((k, v) -> {
                        response.addKey(k);
                        response.addValue(v);
                        localData.remove(k);
                    });
                }
            }
            logger.debug("{} moves {} keys to {}", node.id, response.getKeyCount(), calculateSHA1(request.getSenderIp()+":"+request.getSenderPort(), m));
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        @Override
        public void put(Chord.PutRequest request, StreamObserver<Chord.PutResponse> responseObserver) {
            localData.put(request.getId(), request.getValue());
            logger.debug("<{},{}> saved on {}", request.getId(), request.getValue(), node.id);
        }

        @Override
        public void get(Chord.GetRequest request, StreamObserver<Chord.GetResponse> responseObserver) {
            String value = localData.get(request.getId());
            logger.debug("n:{} returning <{},{}>", node.id, request.getId(), value);
            Chord.GetResponse response = Chord.GetResponse.newBuilder()
                    .setValue(value)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void notify(Chord.Notification request, StreamObserver<Chord.NotificationResponse> responseObserver) {
            logger.debug("[{}:{}] existence of node [{}:{}:{}] was notified", node, node.id, request.getSenderIp(), request.getSenderPort(), request.getId());
            logger.debug("[{}:{}] will set P={}, if {} E ({}, {})", node, node.id, request.getId(), request.getId(), predecessor.id, node.id);
            if (predecessor == node || inRange_OpenOpen(request.getId(), predecessor.id, node.id)) {
                predecessor = new NodeReference(request.getSenderIp(), request.getSenderPort());
            }
            responseObserver.onNext(Chord.NotificationResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void updatePredecessor(Chord.UpdatePredecessorRequest request, StreamObserver<Chord.UpdatePredecessorResponse> responseObserver) {
            logger.debug("[{}:{}] will set P={} (instead of p={}) from {}", node, node.id, request.getPredecessorPort(), predecessor, request.getSenderPort());
            predecessor = new NodeReference(request.getPredecessorIp(), request.getPredecessorPort());
            responseObserver.onNext(Chord.UpdatePredecessorResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void updateSuccessor(Chord.UpdateSuccessorRequest request, StreamObserver<Chord.UpdateSuccessorRequest> responseObserver) {
            logger.debug("[{}:{}] will set S={} (instead of s={}) from {}", node, node.id, request.getSuccessorPort(), fingerTable.get(0).node, request.getSenderPort());
            fingerTable.get(0).setNode(new NodeReference(request.getSuccessorIp(), request.getSuccessorPort()));
            responseObserver.onNext(Chord.UpdateSuccessorRequest.newBuilder().build());
            responseObserver.onCompleted();
        }

        @Override
        public void moveKeysToSuccessor(Chord.MoveKeysToSuccessorRequest request, StreamObserver<Chord.MoveKeysToSuccessorResponse> responseObserver) {
            for (int i = 0; i < request.getKeyCount(); i++) {
                localData.put(request.getKey(i), request.getValue(i));
            }
            responseObserver.onNext(Chord.MoveKeysToSuccessorResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    public static void main(String[] args) throws Exception {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.startServer();

        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.startServer();
        node2.join(bootstrap);

        ChordNode node3 = new ChordNode("localhost", 8982);
        node3.startServer();
        node3.join(node2);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    node2.stopServer();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 5000); // delay in milliseconds

        bootstrap.blockUntilShutdown();
        node2.blockUntilShutdown();
        node3.blockUntilShutdown();



//        ChordNode bootstrap = new ChordNode("localhost", 8980);
//        ChordNode node2 = new ChordNode("localhost", 8981);
//        bootstrap.start();
//        node2.start();
//
//        node2.join(bootstrap.node);
//
//        bootstrap.blockUntilShutdown();
//        node2.blockUntilShutdown();







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
