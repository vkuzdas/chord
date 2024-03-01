import chord.ChordNode;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChordNodeTest {

    @Test
    public void moveKeys() throws Exception {
        assertEquals(ChordNode.m, 4); // Chord space assumption for the test

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
    }

    @Test
    public void threeJoinOnBootstrap() throws Exception {
        assertEquals(ChordNode.m, 4); // Chord space assumption for the test
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.startServer();

        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.startServer();
        node2.join(bootstrap);

        ChordNode node3 = new ChordNode("localhost", 8982);
        node3.startServer();
        node3.join(bootstrap);

        assertEquals(bootstrap.getNodeReference().id, 10);
        assertEquals(node2.getNodeReference().id, 0);
        assertEquals(node3.getNodeReference().id, 2);

        int timeoutSeconds = 2;

        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id == 2);
        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id == 0);

        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id == 10);
        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id == 2);

        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id == 0);
        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id == 10);
    }

    @Test
    public void chainedJoin() throws Exception {
        assertEquals(ChordNode.m, 4); // Chord space assumption for the test
        ChordNode bootstrap = new ChordNode("localhost", 8980);
        bootstrap.startServer();

        ChordNode node2 = new ChordNode("localhost", 8981);
        node2.startServer();
        node2.join(bootstrap);

        ChordNode node3 = new ChordNode("localhost", 8982);
        node3.startServer();
        node3.join(node2);

        assertEquals(bootstrap.getNodeReference().id, 10);
        assertEquals(node2.getNodeReference().id, 0);
        assertEquals(node3.getNodeReference().id, 2);

        int timeoutSeconds = 2;

        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getPredecessor().id == 2);
        await().atMost(timeoutSeconds, SECONDS).until(() -> bootstrap.getSuccessor().id == 0);

        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getPredecessor().id == 10);
        await().atMost(timeoutSeconds, SECONDS).until(() -> node2.getSuccessor().id == 2);

        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getPredecessor().id == 0);
        await().atMost(timeoutSeconds, SECONDS).until(() -> node3.getSuccessor().id == 10);
    }

}
