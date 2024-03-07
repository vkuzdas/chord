import chord.ChordNode;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import java.io.IOException;
import java.math.BigInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * There are some unstable tests, if not sure about the result, run the test separately fixes it
 * You can also try to change logging level to a higher one since excessive logging seems to have an effect
 */
class ChordNodeTest {

    static int FIX_FINGER_TIMEOUT;
    private ArrayList<ChordNode> toShutdown = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        ChordNode.m = 4; // Chord space assumption for the test
        ChordNode.STABILIZATION_INTERVAL = 500; // Chord space assumption for the test
        FIX_FINGER_TIMEOUT = ChordNode.m * ChordNode.STABILIZATION_INTERVAL;
    }

    @BeforeEach
    void init(TestInfo testInfo) throws InterruptedException {
        System.out.println(System.lineSeparator() + System.lineSeparator()
                + "============== " + testInfo.getTestMethod().map(Method::getName).orElse(null)
                + "() =============" + System.lineSeparator());
    }

    @AfterEach
    void tearDown() {
        toShutdown.forEach(ChordNode::awaitStopServer);
    }

    private void registerForShutdown(ChordNode ... nodes) {
        Collections.addAll(toShutdown, nodes);
    }

    @Test
    void testDelete() throws IOException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        registerForShutdown(bootstrap, node2);

        bootstrap.startServer();
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

    @Test // unstable
    void testMoveKeys() throws IOException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        registerForShutdown(bootstrap, node2);

        bootstrap.startServer();

        bootstrap.put("icecream", "sweet");
        bootstrap.put("lollipop", "sour");

        assertEquals(bootstrap.getDataSize(), 2);

        node2.startServer();
        node2.join(bootstrap);

        assertEquals(bootstrap.getDataSize(), 1);
        assertEquals(node2.getDataSize(), 1);

        bootstrap.stopServer();
        node2.stopServer();
    }

    @Test
    void testThreeJoinOnBootstrap() throws Exception {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.startServer();

        node2.startServer();
        node2.join(bootstrap);

        node3.startServer();
        node3.join(bootstrap);

        assertEquals(bootstrap.getNodeReference().id, BigInteger.valueOf(10));
        assertEquals(node2.getNodeReference().id, BigInteger.valueOf(0));
        assertEquals(node3.getNodeReference().id, BigInteger.valueOf(2));

        int timeoutSeconds = 3;

        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id.equals(BigInteger.valueOf(2)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id.equals(BigInteger.valueOf(0)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id.equals(BigInteger.valueOf(10)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id.equals(BigInteger.valueOf(2)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id.equals(BigInteger.valueOf(0))); // not fullfilled
        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id.equals(BigInteger.valueOf(10)));

        bootstrap.stopServer();
        node2.stopServer();
        node3.stopServer();
    }

    @Test
    void testChainedJoin() throws IOException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.startServer();

        node2.startServer();
        node2.join(bootstrap);

        node3.startServer();
        node3.join(node2);

        assertEquals(bootstrap.getNodeReference().id, BigInteger.valueOf(10));
        assertEquals(node2.getNodeReference().id, BigInteger.valueOf(0));
        assertEquals(node3.getNodeReference().id, BigInteger.valueOf(2));

        int timeoutSeconds = 2;

        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id.equals(BigInteger.valueOf(2)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id.equals(BigInteger.valueOf(0)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id.equals(BigInteger.valueOf(10)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id.equals(BigInteger.valueOf(2)));

        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id.equals(BigInteger.valueOf(0)));
        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id.equals(BigInteger.valueOf(10)));

        bootstrap.stopServer();
        node2.stopServer();
        node3.stopServer();
    }

    @Test
    void testLeaveFromBootstrap() throws IOException, InterruptedException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.startServer();

        node2.startServer();
        node2.join(bootstrap);

        node3.startServer();
        node3.join(bootstrap);

        Thread.sleep(FIX_FINGER_TIMEOUT); // let the network stabilize
        node2.leave();
        node2.stopServer();

        // assert that there are no references to the node that left
        // FIXME: this does not validate FingerTable eradication since it just so happens to not be there
        assertNotEquals(bootstrap.getPredecessor(), node2.getNodeReference());
        assertFalse(bootstrap.containedInFingerTable(node2.getNodeReference()));

        assertNotEquals(node3.getPredecessor(), node2.getNodeReference());
        assertFalse(node3.containedInFingerTable(node2.getNodeReference()));

        bootstrap.stopServer();
        node3.stopServer();
    }

    @Test
    void testChainedLeave() throws IOException, InterruptedException {
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        ChordNode node2 = new ChordNode("localhost", 8981);
        ChordNode node3 = new ChordNode("localhost", 8982);
        registerForShutdown(bootstrap, node2, node3);

        bootstrap.startServer();

        node2.startServer();
        node2.join(bootstrap);

        node3.startServer();
        node3.join(node2);

        Thread.sleep(FIX_FINGER_TIMEOUT); // let the network stabilize
        node2.stopServer();
        node2.leave();


        // FIXME: this does not validate FingerTable eradication since it just so happens to not be there
        assertNotEquals(bootstrap.getPredecessor(), node2.getNodeReference());
        assertFalse(bootstrap.containedInFingerTable(node2.getNodeReference()));

        assertNotEquals(node3.getPredecessor(), node2.getNodeReference());
        assertFalse(node3.containedInFingerTable(node2.getNodeReference()));


        bootstrap.stopServer();
        node3.stopServer();
    }

}
