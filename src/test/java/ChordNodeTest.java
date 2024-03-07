import chord.ChordNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class ChordNodeTest {

    static int FIX_FINGER_TIMEOUT;

    @BeforeAll
    static void setUp() {
        ChordNode.m = 4; // Chord space assumption for the test
        ChordNode.STABILIZATION_INTERVAL = 500; // Chord space assumption for the test
        FIX_FINGER_TIMEOUT = ChordNode.m * ChordNode.STABILIZATION_INTERVAL;
    }

    @Test
    void delete() throws Exception {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.startServer();
        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.startServer();
        node2.join(bootstrap);

        node2.put("icecream", "sweet");
        bootstrap.put("lollipop", "sour");

        assertEquals(bootstrap.getDataSize() + node2.getDataSize(), 2);

        bootstrap.delete("icecream");
        bootstrap.delete("lollipop");

        assertEquals(bootstrap.getDataSize() + node2.getDataSize(), 0);

        bootstrap.stopServer();
        node2.stopServer();
    }

    @Test
    void moveKeys() throws Exception {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.startServer();

        bootstrap.put("icecream", "sweet");
        bootstrap.put("lollipop", "sour");

        assertEquals(bootstrap.getDataSize(), 2);

        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.startServer();
        node2.join(bootstrap);


        assertEquals(bootstrap.getDataSize(), 1);
        assertEquals(node2.getDataSize(), 1);

        bootstrap.stopServer();
        node2.stopServer();
    }

//    @Test
//    @Disabled
//    void threeJoinOnBootstrap() throws Exception {
//        ChordNode bootstrap = new ChordNode("localhost", 8980);
//        bootstrap.startServer();
//
//        ChordNode node2 = new ChordNode("localhost", 8981);
//        node2.startServer();
//        node2.join(bootstrap);
//
//        ChordNode node3 = new ChordNode("localhost", 8982);
//        node3.startServer();
//        node3.join(bootstrap);
//
//        assertEquals(bootstrap.getNodeReference().id, 10);
//        assertEquals(node2.getNodeReference().id, 0);
//        assertEquals(node3.getNodeReference().id, 2);
//
//        int timeoutSeconds = 2;
//
//        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id == 2);
//        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id == 0);
//
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id == 10);
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id == 2);
//
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id == 0);
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id == 10);
//
//        bootstrap.stopServer();
//        node2.stopServer();
//        node3.stopServer();
//    }
//
//    @Test
//    @Disabled
//    void chainedJoin() throws Exception {
//        ChordNode bootstrap = new ChordNode("localhost", 8980);
//        bootstrap.startServer();
//
//        ChordNode node2 = new ChordNode("localhost", 8981);
//        node2.startServer();
//        node2.join(bootstrap);
//
//        ChordNode node3 = new ChordNode("localhost", 8982);
//        node3.startServer();
//        node3.join(node2);
//
//        assertEquals(bootstrap.getNodeReference().id, 10);
//        assertEquals(node2.getNodeReference().id, 0);
//        assertEquals(node3.getNodeReference().id, 2);
//
//        int timeoutSeconds = 2;
//
//        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id == 2);
//        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id == 0);
//
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id == 10);
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id == 2);
//
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id == 0);
//        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id == 10);
//
//        bootstrap.stopServer();
//        node2.stopServer();
//        node3.stopServer();
//    }
//
//    @Test
//    @Disabled
//    void leaveFromBootstrap() throws Exception {
//        ChordNode bootstrap = new ChordNode("localhost", 8980);
//        bootstrap.startServer();
//
//        ChordNode node2 = new ChordNode("localhost", 8981);
//        node2.startServer();
//        node2.join(bootstrap);
//
//        ChordNode node3 = new ChordNode("localhost", 8982);
//        node3.startServer();
//        node3.join(bootstrap);
//
//        Thread.sleep(FIX_FINGER_TIMEOUT); // let the network stabilize
//        node2.leave();
//        node2.stopServer();
//
//        // assert that there are no references to the node that left
//        // FIXME: this does not validate FingerTable eradication since it just so happens to not be there
//        assertNotEquals(bootstrap.getPredecessor(), node2.getNodeReference());
//        assertFalse(bootstrap.containedInFingerTable(node2.getNodeReference()));
//
//        assertNotEquals(node3.getPredecessor(), node2.getNodeReference());
//        assertFalse(node3.containedInFingerTable(node2.getNodeReference()));
//
//        bootstrap.stopServer();
//        node3.stopServer();
//    }
//
//    @Test
//    @Disabled
//    void chainedLeave() throws Exception {
//        ChordNode bootstrap = new ChordNode("localhost", 8980);
//        bootstrap.startServer();
//
//        ChordNode node2 = new ChordNode("localhost", 8981);
//        node2.startServer();
//        node2.join(bootstrap);
//
//        ChordNode node3 = new ChordNode("localhost", 8982);
//        node3.startServer();
//        node3.join(node2);
//
//        Thread.sleep(FIX_FINGER_TIMEOUT); // let the network stabilize
//        node2.stopServer();
//        node2.leave();
//
//
//        // FIXME: this does not validate FingerTable eradication since it just so happens to not be there
//        assertNotEquals(bootstrap.getPredecessor(), node2.getNodeReference());
//        assertFalse(bootstrap.containedInFingerTable(node2.getNodeReference()));
//
//        assertNotEquals(node3.getPredecessor(), node2.getNodeReference());
//        assertFalse(node3.containedInFingerTable(node2.getNodeReference()));
//
//
//        bootstrap.stopServer();
//        node3.stopServer();
//    }

}
