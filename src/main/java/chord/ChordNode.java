package chord;

import com.google.common.annotations.VisibleForTesting;
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
import java.util.concurrent.locks.ReentrantLock;

import static chord.Util.*;
import static java.lang.Math.pow;

public class ChordNode {

    private static final Logger logger = LoggerFactory.getLogger(ChordNode.class);

    private final ReentrantLock lock = new ReentrantLock();

    private NodeReference predecessor;

    private final NodeReference node;

    private final ArrayList<Finger> fingerTable = new ArrayList<>();

    private final NavigableMap<Integer, String> localData = new TreeMap<>();

    private Timer stabilizationTimer;

    private TimerTask stabilizationTimerTask;

    //    private static final int m = 160; // TODO: number of bits in id as well as max size of fingerTable
    public static int m = 4; // 0-255 ids

    public static int STABILIZATION_INTERVAL = 500;

    private final Server server;

    private ChordServiceGrpc.ChordServiceBlockingStub blockingStub;


    public ChordNode(String ip, int port) {
        this.node = new NodeReference(ip, port);
        this.predecessor = node;

        for (int i = 1; i <= m; i++) {
            int mod = (int)pow(2, m);
            int start = (node.id + (int)pow(2, i-1)) % mod;
            int end =   (node.id + (int)pow(2, i  )) % mod;
            fingerTable.add(new Finger(start, end, node));
        }

        server = ServerBuilder.forPort(port)
                .addService(new ChordNode.ChordServiceImpl())
                .build();
    }

    public void startServer() throws Exception {
        server.start();
        logger.trace("Server started, listening on {}", node.port);
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            logger.warn("*** shutting down gRPC server on {} since JVM is shutting down", this.node);
//            try {
//                stopServer();
//            } catch (InterruptedException e) {
//                logger.warn("*** server shut down interrupted");
//                e.printStackTrace(System.err);
//            }
//            logger.warn("*** server shut down on {}", this.node);
//        }));
    }

    public void stopServer() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    public void awaitStopServer()  {
        if (server != null) {
            try {
                server.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

    private void syncUpdatePredecessor(NodeReference newPredecessor) {
        lock.lock();
        try {
            predecessor = newPredecessor;
        } finally {
            lock.unlock();
        }
    }

    private void syncUpdateFingerTable(int index, NodeReference newFinger) {
        lock.lock();
        try {
            fingerTable.get(index).setNode(newFinger);
        } finally {
            lock.unlock();
        }
    }

    public void put(String key, String value) {
        int id = calculateSHA1(key, m);
        if(inRange_OpenClose(id, predecessor.id, node.id)) {
            localData.put(id, value);
            logger.debug("<{},{}> saved on {}", id, value, node.id);
        } else {
            NodeReference n_ = findSuccessor(id);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
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
        if(inRange_OpenClose(id, predecessor.id, node.id)) {
            String value = localData.get(id);
            logger.debug("n:{} returning <{},{}>", node.id, id, value);
            return value;
        } else {
            NodeReference n_ = findSuccessor(id);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
            Chord.GetRequest request = Chord.GetRequest.newBuilder()
                    .setId(id)
                    .build();
            Chord.GetResponse response = blockingStub.get(request);
            channel.shutdown();
            return response.getValue();
        }
    }

    public void delete(String key) {
        int id = calculateSHA1(key, m);
        NodeReference n_ = findSuccessor(id);
        if (inRange_OpenClose(id, predecessor.id, node.id)) {
            logger.debug("n:{} removed <{},{}>", node.id, id, localData.get(id));
            localData.remove(id);
        } else {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
            blockingStub = ChordServiceGrpc.newBlockingStub(channel);
            Chord.DeleteRequest request = Chord.DeleteRequest.newBuilder()
                    .setId(id)
                    .build();
            blockingStub.delete(request);
            channel.shutdown();
        }
    }

    public void createRing() {
        startFixThread();
    }

    // Called on X from client
    public void join(ChordNode n_) {
        initFingerTable(n_.node);
        updateOthers();
        moveKeys_RPC(); //from the range (predecessor,n] from successor
        startFixThread();
    }

    /**
     * Set S and P of immediate neighbors to your address
     */
    private void updateNeighborsJoining_RPC() {
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();

        // node.S.P = node
        Chord.UpdateSuccessorRequest.Builder successorRequest = Chord.UpdateSuccessorRequest.newBuilder()
                .setNewIp(node.ip)
                .setNewPort(node.port);
        ChordServiceGrpc.newBlockingStub(channel).updateSuccessor(successorRequest.build());
        channel.shutdown();

        NodeReference predecessor = this.predecessor;
        channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();

        // node.P.S = node
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdatePredecessorRequest.Builder predecessorRequest = Chord.UpdatePredecessorRequest.newBuilder()
                .setNewIp(node.ip)
                .setNewPort(node.port);
        blockingStub.updatePredecessor(predecessorRequest.build());
        channel.shutdown();
    }

    /**
     * Upon leaving, node should be deleted from other nodes that store its reference
     * This means setting this.node.Predecessor.Successor, this.node.Successor.Predecessor
     * and finally any fingertable that this.node might be included in
     */
    public void leave() {
        stopFixThread();
        // no need to delete n from FingerTables since periodic fix_fingers will do it
        updateNeighborsWhenLeaving_RPC();
        if(!localData.isEmpty())
            moveKeysToSuccessor_RPC();

        logger.debug("Node [{}:{}] left the network", node, node.id);
    }

    /**
     * Update S and P of your neighbors when leaving
     */
    private void updateNeighborsWhenLeaving_RPC() {
        // TODO: should I notify them to stop stabilize for a while?, maybe stopping timer for a while
        // update S.P = this.node.P
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdateSuccessorRequest usr = Chord.UpdateSuccessorRequest.newBuilder()
                .setNewIp(this.predecessor.ip)
                .setNewPort(this.predecessor.port)
                .build();
        blockingStub.updateSuccessor(usr);
        channel.shutdown();

        // update P.S = this.node.S
        NodeReference predecessor = this.predecessor;
        channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.UpdatePredecessorRequest upr = Chord.UpdatePredecessorRequest.newBuilder()
                .setNewIp(successor.ip)
                .setNewPort(successor.port)
                .build();
        blockingStub.updatePredecessor(upr);
        channel.shutdown();
    }

    // all nodes from n are moved to n.successor
    private void moveKeysToSuccessor_RPC() {
        NodeReference successor = fingerTable.get(0).node;
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
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
        logger.trace("[{}:{}]  ==== FT ====", node, node.id);
        for (int i = 0; i < fingerTable.size(); i++) {
            Finger finger = fingerTable.get(i);
            logger.trace("Index: {}, [{},{}), Succ: [{}:{}]", i, finger.start, finger.end, finger.node, finger.node.id);
        }
        logger.trace("=====================");
    }

    private void startFixThread() {
        logger.warn("[{}]  started FIX", node);
        // periodic stabilization
        stabilizationTimer = new Timer();
        stabilizationTimerTask = new TimerTask() {
            @Override
            public void run() {
                printStatus();
                fix_fingers();
                stabilize();
                printStatus();
            }
        };
        stabilizationTimer.schedule(stabilizationTimerTask,1000, STABILIZATION_INTERVAL);
    }

    private void stopFixThread() {
        if (stabilizationTimer != null) {
            stabilizationTimer.cancel();
            stabilizationTimer.purge();
            stabilizationTimerTask.cancel();
        }
    }

    private void fix_fingers() {
        int i = new Random().nextInt(m-1)+1;
        syncUpdateFingerTable(i, findSuccessor(fingerTable.get(i).start));
    }

    private void stabilize() {
        // ask S for S.P, decide whether to set n.P = S.P instead
        NodeReference s = fingerTable.get(0).node;
        if (s.equals(node)) {
            logger.debug("[{}:{}]  chord ring appears to be empty", node, node.id);
            return;
        }
        NodeReference s_p = getPredecessor_RPC(s);
        logger.debug("[{}:{}]  s_p {}:{} ?E ({}, {})", node, node.id, s_p, s_p.id, node.id, s.id);
        if (inRange_OpenOpen(s_p.id, node.id, s.id)) {
            syncUpdateFingerTable(0, s_p);
        }
        // notify n.S of n's existence
        logger.debug("[{}:{}] existence notified to [{}:{}]", node, node.id, s, s.id);
        ManagedChannel channel = ManagedChannelBuilder.forTarget(s.getAddress()).usePlaintext().build();
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
        ManagedChannel channel = ManagedChannelBuilder.forTarget(successor.getAddress()).usePlaintext().build();
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

    /**
     * Update all nodes whose finger table should refer to this node
     */
    private void updateOthers() {
        // update ith finger of p node
        for (int i = 0; i < m; i++) {
            int id = node.id - (int)pow(2, i-1);
            if (id < 0) { // wraparound
                id = id + (int)pow(2, m);
            }
            NodeReference p = findPredecessor(id);
            logger.debug("[{}:{}]  pred of {} is {}:{}, i={}", node, node.id, id, p, calculateSHA1(p.getAddress(), m), i);
            updateFingerTableOf_RPC(p, node, i);
        }
    }

    private void updateFingerTableOf_RPC(NodeReference predecessor, NodeReference sender, int index) {
        Chord.UpdateFingerTableRequest req = Chord.UpdateFingerTableRequest.newBuilder()
                .setSenderIp(node.ip)
                .setSenderPort(node.port)
                .setIndex(index)
                .setNodeIp(sender.ip)
                .setNodePort(sender.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(predecessor.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        blockingStub.updateFingerTable(req);
        channel.shutdown();
    }


    private void initFingerTable(NodeReference n_) {
        int targetId = fingerTable.get(0).start;
        NodeReference S = findSuccessor_RPC(n_, targetId);
        syncUpdateFingerTable(0, S);

        syncUpdatePredecessor(getPredecessor_RPC(S));

        updateNeighborsJoining_RPC();

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
        ManagedChannel channel = ManagedChannelBuilder.forTarget(targetNode.getAddress()).usePlaintext().build();
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
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
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

    /**
     * Successor of id is responsible for id (stores it locally) <br>
     * It is found by moving forward around the ChordRing toward most immediately preceding node of id using {@link ChordNode#findPredecessor(int) findPredecessor} <br>
     * Once the most immediate predecessor of id is found, we just return this node successor, therefore finding id's sucessor <br>
     */
    public NodeReference findSuccessor(int id) {
        NodeReference n_ = findPredecessor(id);
        return getSuccessor_RPC(n_);
    }

    /**
     * Prompt contact node to return it's successor
     */
    public NodeReference getSuccessor_RPC(NodeReference n_) {
        if (n_.equals(node)) {
            return fingerTable.get(0).node;
        }
        Chord.GetSuccessorRequest req = Chord.GetSuccessorRequest
                .newBuilder()
                .setRequestorIp(this.node.ip)
                .setRequestorPort(this.node.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.GetSuccessorResponse response = blockingStub.getSuccessor(req);
        channel.shutdown();
        return new NodeReference(response.getSuccessorIp(), response.getSuccessorPort());
    }

    /**
     * Contacts series of nodes moving around the ChordRing towards id
     */
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
            logger.debug("[{}:{}] findPredecessor: {} !E [{}, {})", node, node.id, id, n_.id, S.id);
            n_ = closestPrecedingFingerOf(n_, id);
            S = getSuccessor_RPC(n_);
        }
//        logger.debug("findPredecessor: {} E [{}, {})", id, n_.id, S.id);
        return n_;
    }

    /**
     * Either find closest preceding finger of id from this node's finger table
     * or prompt contact node to return it's closest preceding finger of id
     */
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

    /**
     * Prompt contact node to return closest preceding finger of id from it's finger table
     */
    private NodeReference closestPrecedingFinger_RPC(NodeReference n_, int id) {
        Chord.ClosestPrecedingFingerRequest cpfr = Chord.ClosestPrecedingFingerRequest
                .newBuilder()
                .setTargetId(id)
                .setSenderIp(this.node.ip)
                .setSenderPort(this.node.port)
                .build();
        ManagedChannel channel = ManagedChannelBuilder.forTarget(n_.getAddress()).usePlaintext().build();
        logger.debug("[{}] asking node [{}] for closestPrecedingFinger of id={}", node, n_, id);
        blockingStub = ChordServiceGrpc.newBlockingStub(channel);
        Chord.ClosestPrecedingFingerResponse response = blockingStub.closestPrecedingFinger(cpfr);
        channel.shutdown();
        NodeReference cpf = new NodeReference(response.getClosestPrecedingFingerIp(), response.getClosestPrecedingFingerPort());
        logger.debug("[{}] got {}", node, cpf);
        return cpf;
    }




    /** Procedures served to other nodes */
    private class ChordServiceImpl extends ChordServiceGrpc.ChordServiceImplBase {

        /**
         * Requestor wants this.node to find successor of targetId
         */
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
        }

        /**
         * return this.successor
         */
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

        /**
         * return this.predecessor
         */
        @Override
        public void getPredecessor(Chord.GetPredecessorRequest request, StreamObserver<Chord.GetPredecessorResponse> responseObserver) {
            Chord.GetPredecessorResponse response = Chord.GetPredecessorResponse.newBuilder()
                    .setPredecessorIp(predecessor.ip)
                    .setPredecessorPort(predecessor.port)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * this.node prompted to insert sending node into its finger table
         */
        @Override
        public void updateFingerTable(Chord.UpdateFingerTableRequest request, StreamObserver<Chord.UpdateFingerTableResponse> responseObserver) {
            int index = request.getIndex();
            NodeReference sender = new NodeReference(request.getNodeIp(), request.getNodePort());
            NodeReference i = fingerTable.get(index).node;
            if (inRange_CloseOpen(sender.id, node.id, i.id)) {
                syncUpdateFingerTable(index, sender);
                updateFingerTableOf_RPC(predecessor, sender, index);
            }
            Chord.UpdateFingerTableResponse response = Chord.UpdateFingerTableResponse.newBuilder()
                    .setStatus("OK")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        /**
         * Requestor prompted this.node to return closest preceding finger of id (most immediate predecessor from it's point of view)
         */
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

        /**
         * this.node is prompted to tranfer its keys to requestor (requestor has joined)
         */
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

        /**
         * distributedHashTable.put()
         */
        @Override
        public void put(Chord.PutRequest request, StreamObserver<Chord.PutResponse> responseObserver) {
            localData.put(request.getId(), request.getValue());
            logger.debug("<{},{}> saved on {}", request.getId(), request.getValue(), node.id);
            responseObserver.onNext(Chord.PutResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * distributedHashTable.get()
         */
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

        /**
         * distributedHashTable.remove()
         */
        @Override
        public void delete(Chord.DeleteRequest request, StreamObserver<Chord.DeleteResponse> responseObserver) {
            int id = request.getId();
            logger.debug("n:{} removing <{},{}>", node.id, id, localData.get(id));
            localData.remove(id);
            responseObserver.onNext(Chord.DeleteResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * this.node was notified about predecessor's existence, will set P to new predecessor
         */
        @Override
        public void notify(Chord.Notification request, StreamObserver<Chord.NotificationResponse> responseObserver) {
            logger.debug("[{}:{}] existence of node [{}:{}:{}] was notified", node, node.id, request.getSenderIp(), request.getSenderPort(), request.getId());
            logger.debug("[{}:{}] will set P={}, if {} E ({}, {})", node, node.id, request.getId(), request.getId(), predecessor.id, node.id);
            if (predecessor == node || inRange_OpenOpen(request.getId(), predecessor.id, node.id)) {
                syncUpdatePredecessor(new NodeReference(request.getSenderIp(), request.getSenderPort()));
            }
            responseObserver.onNext(Chord.NotificationResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * RPC called from this.Successor
         * @param request to change Predecessor to newIp and newPort
         */
        @Override
        public void updatePredecessor(Chord.UpdatePredecessorRequest request, StreamObserver<Chord.UpdatePredecessorResponse> responseObserver) {
            NodeReference oldSucc = fingerTable.get(0).node;
            logger.debug("[{}:{}]  will set S={} (prev_s={})", node, node.id, request.getNewPort(), oldSucc);

            // update Successor
            syncUpdateFingerTable(0, new NodeReference(request.getNewIp(), request.getNewPort()));

            // TODO: remove from fingertable
            responseObserver.onNext(Chord.UpdatePredecessorResponse.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * RPC called from this.Predecessor
         * @param request to change Predecessor to newIp and newPort
         */
        @Override
        public void updateSuccessor(Chord.UpdateSuccessorRequest request, StreamObserver<Chord.UpdateSuccessorRequest> responseObserver) {
            syncUpdatePredecessor(new NodeReference(request.getNewIp(), request.getNewPort()));

            // TODO: remove from fingertable??

            responseObserver.onNext(Chord.UpdateSuccessorRequest.newBuilder().build());
            responseObserver.onCompleted();
        }

        /**
         * Preceding node leaves network and transfers its keys to this.node
         * @param request
         * @param responseObserver
         */
        @Override
        public void moveKeysToSuccessor(Chord.MoveKeysToSuccessorRequest request, StreamObserver<Chord.MoveKeysToSuccessorResponse> responseObserver) {
            for (int i = 0; i < request.getKeyCount(); i++) {
                localData.put(request.getKey(i), request.getValue(i));
            }
            responseObserver.onNext(Chord.MoveKeysToSuccessorResponse.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @VisibleForTesting
    public NodeReference getNodeReference() {
        return node;
    }

    @VisibleForTesting
    public boolean containedInFingerTable(NodeReference n) {
        for (Finger f: this.fingerTable) {
            if(n.equals(f.node))
                return true;
        }
        return false;
    }

    @VisibleForTesting
    public NodeReference getPredecessor() {
        return predecessor;
    }

    @VisibleForTesting
    public NodeReference getSuccessor() {
        return fingerTable.get(0).node;
    }

    public static void main(String[] args) throws Exception {

        // TODO: no node should be able to join without calling createRing first
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.startServer();

        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.startServer();
        node2.join(bootstrap);

        ChordNode node3 = new ChordNode("localhost", 8982);
        node3.startServer();
        node3.join(bootstrap);

        Thread.sleep(5000); // let the network stabilize
        bootstrap.leave();
        Thread.sleep(5000); // let the network stabilize
    }
}
